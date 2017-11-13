package com.forstudy.pc.communicator.Models;

import java.util.Set;

/**
 * Created by pc on 10.03.17.
 */

public class Departments {

    private int id_department;

    private String name;

    private Set<Employees> employees;

    private Set<Employees> managers;

    public Set<Employees> getEmployees() {
        return this.employees;
    }

    public void setEmployees(Set<Employees> employees) {
        this.employees = employees;
    }

    public Set<Employees> getManagers() {
        return this.managers;
    }

    public void setManagers(Set<Employees> managers) {
        this.managers = managers;
    }

    public int getId() {
        return id_department;
    }

    public void setId(int id_deparment) {
        this.id_department = id_deparment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
