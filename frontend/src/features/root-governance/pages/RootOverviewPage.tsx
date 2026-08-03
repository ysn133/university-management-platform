import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import {
  createEstablishment,
  getEstablishments,
  getUniversity,
  rootGovernanceKeys,
} from "../api/root-governance-api";
import { EstablishmentForm, type EstablishmentFormValues } from "../components/EstablishmentForm";
import { ManagementModal } from "../components/ManagementModal";
import { StatusBadge } from "../components/StatusBadge";

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

export function RootOverviewPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [isCreateOpen, setCreateOpen] = useState(false);
  const universityQuery = useQuery({
    queryKey: rootGovernanceKeys.university,
    queryFn: getUniversity,
  });
  const universityId = universityQuery.data?.universityId;
  const establishmentsQuery = useQuery({
    queryKey: rootGovernanceKeys.establishments(universityId ?? "pending"),
    queryFn: () => getEstablishments(universityId!),
    enabled: Boolean(universityId),
  });
  const createMutation = useMutation({
    mutationFn: (values: EstablishmentFormValues) =>
      createEstablishment({ universityId: universityId!, ...values }),
    onSuccess: async (establishment) => {
      await queryClient.invalidateQueries({ queryKey: ["root-governance", "establishments"] });
      setCreateOpen(false);
      navigate(`/management/establishments/${establishment.id}`);
    },
  });

  if (universityQuery.isPending) {
    return <div className="management-state">Loading university governance...</div>;
  }

  if (universityQuery.isError) {
    return (
      <div className="management-state management-state--error">
        <h1>University data is unavailable</h1>
        <p>{errorMessage(universityQuery.error)}</p>
        <button className="secondary-button" onClick={() => void universityQuery.refetch()} type="button">Try again</button>
      </div>
    );
  }

  const establishments = establishmentsQuery.data ?? [];
  const activeCount = establishments.filter((item) => item.status === "ACTIVE").length;
  const inactiveCount = establishments.filter((item) => item.status === "INACTIVE").length;

  return (
    <div className="management-page root-overview-page">
      <header className="management-page-header">
        <div>
          <p className="management-kicker">University command center</p>
          <h1>{universityQuery.data.universityName}</h1>
          <p>Manage the university structure and the establishment leadership responsible for daily operations.</p>
        </div>
        <button className="management-primary-button" onClick={() => setCreateOpen(true)} type="button">
          New establishment
        </button>
      </header>

      <section className="metric-grid" aria-label="University summary">
        <article className="metric-card metric-card--primary">
          <span>Total establishments</span>
          <strong>{establishments.length}</strong>
          <small>Faculties, schools, and institutes</small>
        </article>
        <article className="metric-card">
          <span>Active</span>
          <strong>{activeCount}</strong>
          <small>Available for operational management</small>
        </article>
        <article className="metric-card">
          <span>Inactive</span>
          <strong>{inactiveCount}</strong>
          <small>Temporarily outside active operations</small>
        </article>
      </section>

      <section className="management-panel">
        <header className="panel-header">
          <div>
            <p className="management-kicker">University structure</p>
            <h2>Establishments</h2>
          </div>
          <Link className="text-action" to="/management/establishments">View directory</Link>
        </header>

        {establishmentsQuery.isPending ? (
          <div className="panel-empty">Loading establishments...</div>
        ) : establishmentsQuery.isError ? (
          <div className="panel-empty panel-empty--error">{errorMessage(establishmentsQuery.error)}</div>
        ) : establishments.length === 0 ? (
          <div className="panel-empty">
            <strong>No establishments have been created.</strong>
            <p>Create the first faculty, school, or institute to begin university administration.</p>
            <button className="secondary-button" onClick={() => setCreateOpen(true)} type="button">Create establishment</button>
          </div>
        ) : (
          <div className="establishment-preview-list">
            {establishments.slice(0, 5).map((establishment) => (
              <Link key={establishment.id} to={`/management/establishments/${establishment.id}`}>
                <span className="resource-monogram">{establishment.name.slice(0, 2).toUpperCase()}</span>
                <div>
                  <strong>{establishment.name}</strong>
                  <small>{establishment.type[0] + establishment.type.slice(1).toLowerCase()}</small>
                </div>
                <StatusBadge status={establishment.status} />
                <span className="row-arrow" aria-hidden="true">→</span>
              </Link>
            ))}
          </div>
        )}
      </section>

      {isCreateOpen && universityId && (
        <ManagementModal
          title="Create establishment"
          description="Add a faculty, school, or institute under the university."
          onClose={() => setCreateOpen(false)}
        >
          <EstablishmentForm
            isSubmitting={createMutation.isPending}
            requestError={createMutation.isError ? errorMessage(createMutation.error) : null}
            onCancel={() => setCreateOpen(false)}
            onSubmit={async (values) => { try { await createMutation.mutateAsync(values); } catch { /* shown by mutation state */ } }}
          />
        </ManagementModal>
      )}
    </div>
  );
}
