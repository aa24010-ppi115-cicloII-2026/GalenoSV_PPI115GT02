/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Convert;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary.conversores.UUIDConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 *
 * @author mrxto
 */
@Entity
@Table(name = "procedimiento_paso_examen")
@NamedQueries({
    @NamedQuery(name = "ProcedimientoPasoExamen.findAll", query = "SELECT p FROM ProcedimientoPasoExamen p"),
    @NamedQuery(name = "ProcedimientoPasoExamen.findByFechaCreacion", query = "SELECT p FROM ProcedimientoPasoExamen p WHERE p.fechaCreacion = :fechaCreacion"),
    @NamedQuery(name = "ProcedimientoPasoExamen.findByActivo", query = "SELECT p FROM ProcedimientoPasoExamen p WHERE p.activo = :activo"),
    @NamedQuery(name = "ProcedimientoPasoExamen.findByObservaciones", query = "SELECT p FROM ProcedimientoPasoExamen p WHERE p.observaciones = :observaciones")})
public class ProcedimientoPasoExamen implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    
    @Column(name = "id_procedimiento_paso_examen")
    @Convert(converter = UUIDConverter.class)
    private UUID idProcedimientoPasoExamen;
    @Column(name = "fecha_creacion")
    
    private OffsetDateTime fechaCreacion;
    @Column(name = "activo")
    private Boolean activo;
    @Size(max = 2147483647)
    @Column(name = "observaciones")
    private String observaciones;
    @JoinColumn(name = "id_examen", referencedColumnName = "id_examen")
    @ManyToOne
    private Examen idExamen;
    @JoinColumn(name = "id_procedimiento_paso", referencedColumnName = "id_procedimiento_paso")
    @ManyToOne
    private ProcedimientoPaso idProcedimientoPaso;

    public ProcedimientoPasoExamen() {
    }

    public ProcedimientoPasoExamen(UUID idProcedimientoPasoExamen) {
        this.idProcedimientoPasoExamen = idProcedimientoPasoExamen;
    }

    public UUID getIdProcedimientoPasoExamen() {
        return idProcedimientoPasoExamen;
    }

    public void setIdProcedimientoPasoExamen(UUID idProcedimientoPasoExamen) {
        this.idProcedimientoPasoExamen = idProcedimientoPasoExamen;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(OffsetDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Examen getIdExamen() {
        return idExamen;
    }

    public void setIdExamen(Examen idExamen) {
        this.idExamen = idExamen;
    }

    public ProcedimientoPaso getIdProcedimientoPaso() {
        return idProcedimientoPaso;
    }

    public void setIdProcedimientoPaso(ProcedimientoPaso idProcedimientoPaso) {
        this.idProcedimientoPaso = idProcedimientoPaso;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idProcedimientoPasoExamen != null ? idProcedimientoPasoExamen.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ProcedimientoPasoExamen)) {
            return false;
        }
        ProcedimientoPasoExamen other = (ProcedimientoPasoExamen) object;
        if ((this.idProcedimientoPasoExamen == null && other.idProcedimientoPasoExamen != null) || (this.idProcedimientoPasoExamen != null && !this.idProcedimientoPasoExamen.equals(other.idProcedimientoPasoExamen))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues_occ_ingenieria_pp115_2026_salud.galenosv.entity.ProcedimientoPasoExamen[ idProcedimientoPasoExamen=" + idProcedimientoPasoExamen + " ]";
    }
    
}
