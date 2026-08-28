import { FormEvent, useEffect, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import {
  AiNavigationDiagnostics,
  AiNavigationHistoryMessage,
  AiNavigationRequestError,
  resolveAiNavigation,
} from "../api/ai-navigation-api";

type DebugRequest = {
  browserTotalMs: number;
  currentRoute: string;
  query: string;
  status: number;
};

type ChatMessage = {
  id: number;
  role: "USER" | "ASSISTANT";
  content: string;
  error?: boolean;
  route?: string;
};

export function AiNavigationWidget() {
  const location = useLocation();
  const navigate = useNavigate();
  const [isOpen, setOpen] = useState(false);
  const [query, setQuery] = useState("");
  const [isResolving, setResolving] = useState(false);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [diagnostics, setDiagnostics] = useState<AiNavigationDiagnostics | null>(null);
  const [debugRequest, setDebugRequest] = useState<DebugRequest | null>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);
  const conversationRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!isOpen) return;
    inputRef.current?.focus();

    const closeOnEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape" && !isResolving) setOpen(false);
    };
    window.addEventListener("keydown", closeOnEscape);
    return () => window.removeEventListener("keydown", closeOnEscape);
  }, [isOpen, isResolving]);

  useEffect(() => {
    if (conversationRef.current) {
      conversationRef.current.scrollTop = conversationRef.current.scrollHeight;
    }
  }, [messages, isResolving]);

  const reset = () => {
    setQuery("");
    setMessages([]);
    setDiagnostics(null);
    setDebugRequest(null);
    window.setTimeout(() => inputRef.current?.focus(), 0);
  };

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const request = query.trim();
    if (!request || isResolving) return;

    const currentRoute = `${location.pathname}${location.search}`;
    const history: AiNavigationHistoryMessage[] = messages
      .filter((message) => !message.error)
      .slice(-5)
      .map((message) => ({
        role: message.role,
        content: message.content,
      }));
    setMessages((current) => [...current, {
      id: Date.now(),
      role: "USER",
      content: request,
    }]);
    setQuery("");
    setDiagnostics(null);
    setDebugRequest(null);
    setResolving(true);
    const browserStarted = performance.now();
    try {
      const response = await resolveAiNavigation(request, currentRoute, history);
      setDiagnostics(response.diagnostics);
      if (response.diagnostics) {
        setDebugRequest({
          browserTotalMs: Math.round(performance.now() - browserStarted),
          currentRoute,
          query: request,
          status: 200,
        });
      }
      if (response.mode === "NAVIGATE" && !response.diagnostics) {
        setMessages((current) => [...current, {
          id: Date.now() + 1,
          role: "ASSISTANT",
          content: response.message,
          route: response.route,
        }]);
        setOpen(false);
        navigate(response.route);
        return;
      }
      setMessages((current) => [...current, {
        id: Date.now() + 1,
        role: "ASSISTANT",
        content: response.message,
        route: response.mode === "NAVIGATE" ? response.route : undefined,
      }]);
    } catch (requestError) {
      if (requestError instanceof AiNavigationRequestError) {
        setDiagnostics(requestError.diagnostics);
        if (requestError.diagnostics) {
          setDebugRequest({
            browserTotalMs: Math.round(performance.now() - browserStarted),
            currentRoute,
            query: request,
            status: requestError.status,
          });
        }
      }
      setMessages((current) => [...current, {
        id: Date.now() + 1,
        role: "ASSISTANT",
        error: true,
        content: requestError instanceof Error
          ? requestError.message
          : "The destination could not be resolved.",
      }]);
    } finally {
      setResolving(false);
    }
  };

  return (
    <div className={`ai-navigator${isOpen ? " is-open" : ""}`}>
      {isOpen && (
        <section aria-label="AI navigation" className="ai-navigator-panel">
          <header className="ai-navigator-header">
            <div className="ai-navigator-mark" aria-hidden="true">
              <svg fill="none" viewBox="0 0 24 24">
                <path d="M12 3.5c.7 4.4 2.1 5.8 6.5 6.5-4.4.7-5.8 2.1-6.5 6.5-.7-4.4-2.1-5.8-6.5-6.5 4.4-.7 5.8-2.1 6.5-6.5Z" />
                <path d="M18.5 15.5c.25 1.65.85 2.25 2.5 2.5-1.65.25-2.25.85-2.5 2.5-.25-1.65-.85-2.25-2.5-2.5 1.65-.25 2.25-.85 2.5-2.5Z" />
              </svg>
            </div>
            <div>
              <div className="ai-navigator-title">
                <h2>Platform assistant</h2>
                <b>Beta</b>
              </div>
              <span><i /> Ready to help</span>
            </div>
            <div className="ai-navigator-header-actions">
              {messages.length > 0 && (
                <button aria-label="Clear chat" className="ai-navigator-clear" disabled={isResolving} onClick={reset} type="button">
                  <svg fill="none" viewBox="0 0 20 20"><path d="M5.5 6.5h9M8 6.5V5h4v1.5m-5.5 0 .6 9h5.8l.6-9M8.5 9v4m3-4v4" /></svg>
                </button>
              )}
              <button
                aria-label="Close AI navigation"
                className="ai-navigator-close"
                disabled={isResolving}
                onClick={() => setOpen(false)}
                type="button"
              >
                <svg fill="none" viewBox="0 0 20 20"><path d="m6 6 8 8M14 6l-8 8" /></svg>
              </button>
            </div>
          </header>

          <form onSubmit={(event) => void submit(event)}>
            <div className="ai-navigator-conversation" ref={conversationRef}>
              {messages.length === 0 && !isResolving && (
                <div className="ai-navigator-welcome">
                  <div className="ai-navigator-welcome-mark" aria-hidden="true">
                    <svg fill="none" viewBox="0 0 24 24">
                      <path d="M12 3.5c.7 4.4 2.1 5.8 6.5 6.5-4.4.7-5.8 2.1-6.5 6.5-.7-4.4-2.1-5.8-6.5-6.5 4.4-.7 5.8-2.1 6.5-6.5Z" />
                    </svg>
                  </div>
                  <h3>What would you like to find?</h3>
                  <p>Ask about students, professors, grades, schedules, or any page in the platform.</p>
                  <div className="ai-navigator-suggestions">
                    <button onClick={() => setQuery("open the grades of the student with the apogee code 2601001")} type="button">Student grades</button>
                    <button onClick={() => setQuery("Show me the professors in this establishment")} type="button">Find professors</button>
                  </div>
                </div>
              )}

              {messages.map((message) => message.role === "USER" ? (
                <div className="ai-message is-user" key={message.id}>
                  <div className="ai-message-bubble">{message.content}</div>
                </div>
              ) : (
                <section
                  aria-label={message.route ? "Resolved destination" : "Answer"}
                  className="ai-message is-assistant"
                  key={message.id}
                >
                  <div className={`ai-message-avatar${message.error ? " is-error" : ""}`} aria-hidden="true">
                    {message.error ? "!" : <span />}
                  </div>
                  <div className="ai-message-content">
                    <p className={message.error ? "ai-navigator-error" : "ai-message-bubble"} role={message.error ? "alert" : undefined}>
                      {message.content}
                    </p>
                    {message.route && (
                      <button className="ai-message-open" onClick={() => {
                        setOpen(false);
                        navigate(message.route!);
                      }} type="button">Open page <span aria-hidden="true">→</span></button>
                    )}
                  </div>
                </section>
              ))}

              {isResolving && (
                <div className="ai-message is-assistant" aria-label="Assistant is responding">
                  <div className="ai-message-avatar" aria-hidden="true"><span /></div>
                  <div className="ai-message-bubble ai-message-typing"><i /><i /><i /></div>
                </div>
              )}

              {debugRequest && diagnostics && (
                <AiNavigationDebugTrace request={debugRequest} diagnostics={diagnostics} />
              )}
            </div>

            <div className="ai-navigator-composer">
                <label className="sr-only" htmlFor="ai-navigation-query">Describe the page or record</label>
                <textarea
                  disabled={isResolving}
                  id="ai-navigation-query"
                  maxLength={500}
                  onChange={(event) => setQuery(event.target.value)}
                  onKeyDown={(event) => {
                    if (event.key === "Enter" && !event.shiftKey) {
                      event.preventDefault();
                      event.currentTarget.form?.requestSubmit();
                    }
                  }}
                  placeholder="Ask about the platform..."
                  ref={inputRef}
                  rows={1}
                  value={query}
                />
                <button aria-label="Send request" disabled={!query.trim() || isResolving} type="submit">
                  {isResolving ? <span className="ai-navigator-spinner" /> : (
                    <svg aria-hidden="true" fill="none" viewBox="0 0 20 20">
                      <path d="m4 10 11-6-3.2 12-2.2-4.1L4 10Z" />
                      <path d="m9.6 11.9 2.8-3.1" />
                    </svg>
                  )}
                </button>
            </div>
          </form>
        </section>
      )}

      {!isOpen && (
        <button className="ai-navigator-trigger" onClick={() => setOpen(true)} type="button">
          <svg aria-hidden="true" fill="none" viewBox="0 0 24 24">
            <path d="M12 3.5c.7 4.4 2.1 5.8 6.5 6.5-4.4.7-5.8 2.1-6.5 6.5-.7-4.4-2.1-5.8-6.5-6.5 4.4-.7 5.8-2.1 6.5-6.5Z" />
            <path d="M18.5 15.5c.25 1.65.85 2.25 2.5 2.5-1.65.25-2.25.85-2.5 2.5-.25-1.65-.85-2.25-2.5-2.5 1.65-.25 2.25-.85 2.5-2.5Z" />
          </svg>
          Ask <span>Beta</span>
        </button>
      )}
    </div>
  );
}

function AiNavigationDebugTrace({
  request,
  diagnostics,
}: {
  request: DebugRequest;
  diagnostics: AiNavigationDiagnostics;
}) {
  const apiCallCount = diagnostics.executions.reduce(
    (total, execution) => total + execution.apiCalls.length,
    0,
  );

  return (
    <section className="ai-navigation-debug" aria-label="Execution trace">
      <header>
        <div><span>Execution trace</span><strong>{request.status < 400 ? "Completed" : "Failed"}</strong></div>
        <dl>
          <div><dt>Model calls</dt><dd>{diagnostics.modelCalls.length}</dd></div>
          <div><dt>API reads</dt><dd>{apiCallCount}</dd></div>
          <div><dt>HTTP</dt><dd>{request.status}</dd></div>
          <div><dt>Browser</dt><dd>{request.browserTotalMs} ms</dd></div>
          <div><dt>Server</dt><dd>{diagnostics.serverTotalMs} ms</dd></div>
        </dl>
      </header>

      <details open>
        <summary>Widget request</summary>
        <pre>{JSON.stringify({
          method: "POST",
          path: "/api/v1/ai/navigation",
          body: { query: request.query, currentRoute: request.currentRoute },
          startedAt: diagnostics.startedAt,
        }, null, 2)}</pre>
      </details>

      <details open>
        <summary>Knowledge retrieval ({diagnostics.retrievals.length})</summary>
        {diagnostics.retrievals.map((retrieval, index) => (
          <article key={`${retrieval.query}-${index}`}>
            <strong>Retrieval {index + 1}</strong>
            <span>{retrieval.durationMs} ms · {retrieval.matchCount} chunks · {retrieval.contextCharacters.toLocaleString()} context characters</span>
            <pre>{retrieval.query}</pre>
            <ul className="ai-navigation-debug-matches">
              {retrieval.matches.map((match, matchIndex) => (
                <li key={`${match.source}-${match.title}-${matchIndex}`}>
                  <b>{match.source}</b><span>{match.title}</span><em>{match.score.toFixed(3)}</em>
                </li>
              ))}
            </ul>
          </article>
        ))}
      </details>

      <details open>
        <summary>Generated plans ({diagnostics.modelCalls.length})</summary>
        {diagnostics.modelCalls.map((modelCall, index) => (
          <article key={`${modelCall.label}-${index}`}>
            <strong>{modelCall.label}</strong><span>{modelCall.durationMs} ms</span>
            <pre>{modelCall.plan === null
              ? "Answer generation call (no navigation plan)"
              : JSON.stringify(modelCall.plan, null, 2)}</pre>
          </article>
        ))}
      </details>

      <details open>
        <summary>Plan execution ({diagnostics.executions.length})</summary>
        {diagnostics.executions.map((execution, executionIndex) => (
          <article key={`${execution.label}-${executionIndex}`}>
            <strong>{execution.label}</strong>
            <span>{execution.status} · {execution.durationMs} ms · {execution.outcome}</span>
            {execution.apiCalls.length === 0 ? <p>No internal API request completed.</p> : execution.apiCalls.map((call, callIndex) => (
              <div className="ai-navigation-debug-call" key={`${call.path}-${callIndex}`}>
                <div><b>GET</b><code>{call.path}</code><em>{call.status}</em></div>
                <small>Query parameters</small>
                <pre>{formatJson(call.queryParameters)}</pre>
                <small>Response / validation preview</small>
                <pre>{formatJson(call.responsePreview)}</pre>
              </div>
            ))}
          </article>
        ))}
      </details>
    </section>
  );
}

function formatJson(value: string) {
  try {
    return JSON.stringify(JSON.parse(value), null, 2);
  } catch {
    return value || "(empty)";
  }
}
