import { useEffect, useState } from "react";
import type { Permission, PermissionCode } from "../api/establishment-admin-api";

interface PermissionGrantFormProps {
  catalog: Permission[];
  grantedPermissions: PermissionCode[];
  isSubmitting: boolean;
  requestError?: string | null;
  onCancel: () => void;
  onSubmit: (permissions: PermissionCode[]) => Promise<void>;
  cancelLabel?: string;
  submitLabel?: string;
}

const groupMatchers = [
  { label: "Academic structure", prefixes: ["DEPARTMENT_", "PROGRAM_", "DEGREE_", "ACADEMIC_LEVEL_", "ACADEMIC_RULE_", "ACADEMIC_YEAR_", "SEMESTER_", "SUBJECT_MODULE_", "CLASS_GROUP_"] },
  { label: "People", prefixes: ["ADMIN_", "STUDENT_", "PROFESSOR_", "ACADEMIC_DOMAIN_"] },
  { label: "Teaching delivery", prefixes: ["MODULE_", "TEACHING_", "ACADEMIC_REGISTRATION_", "ABSENCE_"] },
  { label: "Planning and assessment", prefixes: ["BLOCK_", "ROOM_", "EXAM_", "GRADE_"] },
] as const;

function groupFor(code: string): string {
  return groupMatchers.find((group) => group.prefixes.some((prefix) => code.startsWith(prefix)))?.label ?? "Other";
}

export function PermissionGrantForm({
  catalog,
  grantedPermissions,
  isSubmitting,
  requestError,
  onCancel,
  onSubmit,
  cancelLabel = "Cancel",
  submitLabel = "Save permissions",
}: PermissionGrantFormProps) {
  const [selected, setSelected] = useState<Set<PermissionCode>>(new Set(grantedPermissions));

  useEffect(() => setSelected(new Set(grantedPermissions)), [grantedPermissions]);

  const groups = catalog.reduce<Map<string, Permission[]>>((result, permission) => {
    const group = groupFor(permission.code);
    result.set(group, [...(result.get(group) ?? []), permission]);
    return result;
  }, new Map());

  function toggle(code: PermissionCode) {
    setSelected((current) => {
      const next = new Set(current);
      if (next.has(code)) next.delete(code);
      else next.add(code);
      return next;
    });
  }

  function toggleGroup(permissions: Permission[]) {
    const codes = permissions.map((permission) => permission.code as PermissionCode);
    const allSelected = codes.every((code) => selected.has(code));
    setSelected((current) => {
      const next = new Set(current);
      for (const code of codes) allSelected ? next.delete(code) : next.add(code);
      return next;
    });
  }

  return (
    <form className="permission-form" onSubmit={(event) => { event.preventDefault(); void onSubmit([...selected]); }}>
      <div className="permission-summary">
        <strong>{selected.size}</strong>
        <span>permissions granted</span>
      </div>

      <div className="permission-groups">
        {[...groups.entries()].map(([group, permissions]) => {
          const allSelected = permissions.every((permission) => selected.has(permission.code as PermissionCode));
          return (
            <section className="permission-group" key={group}>
              <header>
                <div><h3>{group}</h3><span>{permissions.length} permissions</span></div>
                <button onClick={() => toggleGroup(permissions)} type="button">{allSelected ? "Clear group" : "Select group"}</button>
              </header>
              <div className="permission-grid">
                {permissions.map((permission) => {
                  const code = permission.code as PermissionCode;
                  return (
                    <label className={selected.has(code) ? "is-selected" : ""} key={permission.code}>
                      <input checked={selected.has(code)} onChange={() => toggle(code)} type="checkbox" />
                      <span><strong>{permission.name}</strong><small>{permission.code}</small></span>
                    </label>
                  );
                })}
              </div>
            </section>
          );
        })}
      </div>

      {requestError && <div className="management-alert management-alert--error">{requestError}</div>}
      <footer className="form-actions permission-actions">
        <button className="secondary-button" onClick={onCancel} type="button">{cancelLabel}</button>
        <button className="management-primary-button" disabled={isSubmitting} type="submit">
          {isSubmitting ? "Saving..." : submitLabel}
        </button>
      </footer>
    </form>
  );
}
