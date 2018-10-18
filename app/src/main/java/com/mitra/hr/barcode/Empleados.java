package com.mitra.hr.barcode;

/**
 * Created by diego.sejas on 17/08/2017.
 */

public class Empleados {
    private Integer IdEmpleado;
    private String Nombre;
    private String Apellido;

    public Integer getIdEmpleado() {
        return IdEmpleado;
    }

    public void setIdEmpleado(Integer idEmpleado) {
        IdEmpleado = idEmpleado;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public String getApellido() {
        return Apellido;
    }

    public void setApellido(String apellido) {
        Apellido = apellido;
    }
}
