package com.example.appgestiontrafico.Modelos;

public class MarcadorDoble {

    private double latitudMarcador1;
    private double longitudMarcador1;
    private double latitudMarcador2;
    private double longitudMarcador2;
    private String horaActualizacion;
    private Boolean activo;
    private String nombre;

    public MarcadorDoble(){

    }

    public double getLatitudMarcador1() {
        return latitudMarcador1;
    }

    public void setLatitudMarcador1(double latitudMarcador1) {
        this.latitudMarcador1 = latitudMarcador1;
    }

    public double getLongitudMarcador1() {
        return longitudMarcador1;
    }

    public void setLongitudMarcador1(double longitudMarcador1) {
        this.longitudMarcador1 = longitudMarcador1;
    }

    public double getLatitudMarcador2() {
        return latitudMarcador2;
    }

    public void setLatitudMarcador2(double latitudMarcador2) {
        this.latitudMarcador2 = latitudMarcador2;
    }

    public double getLongitudMarcador2() {
        return longitudMarcador2;
    }

    public void setLongitudMarcador2(double longitudMarcador2) {
        this.longitudMarcador2 = longitudMarcador2;
    }

    public String getHoraActualizacion() {
        return horaActualizacion;
    }

    public void setHoraActualizacion(String horaActualizacion) {
        this.horaActualizacion = horaActualizacion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
