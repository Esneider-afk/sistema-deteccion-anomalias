package com.empresa.anomalias.model;

public enum EstadoAnomalia {
    PENDIENTE_REVISION("Pendiente por revisión", "bg-warning text-dark"),
    JUSTIFICADA("Justificada", "bg-success"),
    REVISADA("Revisada", "bg-primary");

    private final String etiqueta;
    private final String clase;

    EstadoAnomalia(String etiqueta, String clase) {
        this.etiqueta = etiqueta;
        this.clase = clase;
    }

    public String getEtiqueta() { return etiqueta; }
    public String getClase() { return clase; }
}