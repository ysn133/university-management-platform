import { describe, expect, it } from "vitest";
import { getManagementNavigation } from "./management-navigation";

describe("management navigation", () => {
  it("builds root academic paths inside the selected establishment", () => {
    const navigation = getManagementNavigation("ROOT_SUPER_ADMIN", "establishment-1");
    expect(navigation.find((item) => item.label === "Departments")?.to).toBe("/management/establishments/establishment-1/departments");
  });

  it("exposes academic structure to an establishment Admin", () => {
    const labels = getManagementNavigation("ADMIN", "establishment-1").map((item) => item.label);
    expect(labels).toEqual(expect.arrayContaining(["Professors", "Departments", "Program Paths", "Degree Cycles", "Programs", "Academic Years", "Academic Rules", "Academic Domains"]));
    expect(labels).not.toContain("Admins");
  });
});
