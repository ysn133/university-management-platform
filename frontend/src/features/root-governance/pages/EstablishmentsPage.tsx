import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useDeferredValue, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ApiRequestError } from "@/shared/api/client/ApiRequestError";
import {
  createEstablishment,
  getEstablishments,
  getUniversity,
  rootGovernanceKeys,
  type EstablishmentStatus,
  type EstablishmentType,
} from "../api/root-governance-api";
import { EstablishmentForm, type EstablishmentFormValues } from "../components/EstablishmentForm";
import { ManagementModal } from "../components/ManagementModal";
import { StatusBadge } from "../components/StatusBadge";

function errorMessage(error: unknown): string {
  return error instanceof ApiRequestError ? error.message : "The request could not be completed.";
}

export function EstablishmentsPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [query, setQuery] = useState("");
  const [type, setType] = useState<EstablishmentType | "">("");
  const [status, setStatus] = useState<EstablishmentStatus | "">("");
  const [isCreateOpen, setCreateOpen] = useState(false);
  const deferredQuery = useDeferredValue(query.trim());
  const universityQuery = useQuery({ queryKey: rootGovernanceKeys.university, queryFn: getUniversity });
  const universityId = universityQuery.data?.universityId;
  const filters = {
    ...(deferredQuery ? { query: deferredQuery } : {}),
    ...(type ? { type } : {}),
    ...(status ? { status } : {}),
  };
  const establishmentsQuery = useQuery({
    queryKey: rootGovernanceKeys.establishments(universityId ?? "pending", filters),
    queryFn: () => getEstablishments(universityId!, filters),
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

  return (
    <div className="management-page">
      <header className="management-page-header management-page-header--compact">
        <div>
          <p className="management-kicker">University structure</p>
          <h1>Establishments</h1>
          <p>Manage every faculty, school, and institute governed by the university.</p>
        </div>
        <button className="management-primary-button" disabled={!universityId} onClick={() => setCreateOpen(true)} type="button">
          New establishment
        </button>
      </header>

      <section className="directory-toolbar" aria-label="Establishment filters">
        <label className="search-field">
          <span>Search</span>
          <input onChange={(event) => setQuery(event.target.value)} placeholder="Search by name" value={query} />
        </label>
        <label>
          <span>Type</span>
          <select onChange={(event) => setType(event.target.value as EstablishmentType | "")} value={type}>
            <option value="">All types</option>
            <option value="FACULTY">Faculty</option>
            <option value="SCHOOL">School</option>
            <option value="INSTITUTE">Institute</option>
          </select>
        </label>
        <label>
          <span>Status</span>
          <select onChange={(event) => setStatus(event.target.value as EstablishmentStatus | "")} value={status}>
            <option value="">All statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
            <option value="ARCHIVED">Archived</option>
          </select>
        </label>
      </section>

      <section className="management-panel directory-panel">
        <header className="panel-header panel-header--bordered">
          <div>
            <h2>University directory</h2>
            <p>{establishmentsQuery.data?.length ?? 0} establishments found</p>
          </div>
        </header>

        {universityQuery.isError || establishmentsQuery.isError ? (
          <div className="panel-empty panel-empty--error">
            {errorMessage(universityQuery.error ?? establishmentsQuery.error)}
          </div>
        ) : universityQuery.isPending || establishmentsQuery.isPending ? (
          <div className="panel-empty">Loading establishments...</div>
        ) : establishmentsQuery.data.length === 0 ? (
          <div className="panel-empty">
            <strong>No establishment matches these filters.</strong>
            <p>Adjust the filters or create a new establishment.</p>
          </div>
        ) : (
          <div className="resource-table-wrapper">
            <table className="resource-table">
              <thead>
                <tr><th>Establishment</th><th>Type</th><th>Status</th><th>Created</th><th /></tr>
              </thead>
              <tbody>
                {establishmentsQuery.data.map((establishment) => (
                  <tr key={establishment.id}>
                    <td>
                      <Link className="resource-name" to={`/management/establishments/${establishment.id}`}>
                        <span className="resource-monogram">{establishment.name.slice(0, 2).toUpperCase()}</span>
                        <strong>{establishment.name}</strong>
                      </Link>
                    </td>
                    <td>{establishment.type[0] + establishment.type.slice(1).toLowerCase()}</td>
                    <td><StatusBadge status={establishment.status} /></td>
                    <td>{establishment.createdAt ? new Intl.DateTimeFormat("en-GB", { dateStyle: "medium" }).format(new Date(establishment.createdAt)) : "-"}</td>
                    <td><Link className="table-action" to={`/management/establishments/${establishment.id}`}>Manage</Link></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {isCreateOpen && universityId && (
        <ManagementModal title="Create establishment" description="Add a faculty, school, or institute under the university." onClose={() => setCreateOpen(false)}>
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
