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
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author mrxto
 */
@Entity
@Table(name = "tipo_examen")
@NamedQueries({
    @NamedQuery(name = "TipoExamen.findAll", query = "SELECT t FROM TipoExamen t"),
    @NamedQuery(name = "TipoExamen.findByNombre", query = "SELECT t FROM TipoExamen t WHERE t.nombre = :nombre"),
    @NamedQuery(name = "TipoExamen.findByNombreLike", query = "SELECT t FROM TipoExamen t WHERE UPPER(t.nombre) LIKE :nombre"),
    @NamedQuery(name = "TipoExamen.findByActivo", query = "SELECT t FROM TipoExamen t WHERE t.activo = :activo"),
    @NamedQuery(name = "TipoExamen.findByObservaciones", query = "SELECT t FROM TipoExamen t WHERE t.observaciones = :observaciones")})
public class TipoExamen implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    
    @Column(name = "id_tipo_examen")
    @Convert(converter = UUIDConverter.class)
    private UUID idTipoExamen;
    @Size(max = 2147483647)
    @Column(name = "nombre")
    private String nombre;
    @Column(name = "activo")
    private Boolean activo;
    @Size(max = 2147483647)
    @Column(name = "observaciones")
    private String observaciones;
    @OneToMany(mappedBy = "idTipoExamen")
    private List<ExamenTipoExamen> examenTipoExamenList;

    public TipoExamen() {
    }

    public TipoExamen(UUID idTipoExamen) {
        this.idTipoExamen = idTipoExamen;
    }

    public UUID getIdTipoExamen() {
        return idTipoExamen;
    }

    public void setIdTipoExamen(UUID idTipoExamen) {
        this.idTipoExamen = idTipoExamen;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public List<ExamenTipoExamen> getExamenTipoExamenList() {
        return examenTipoExamenList;
    }

    public void setExamenTipoExamenList(List<ExamenTipoExamen> examenTipoExamenList) {
        this.examenTipoExamenList = examenTipoExamenList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idTipoExamen != null ? idTipoExamen.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TipoExamen)) {
            return false;
        }
        TipoExamen other = (TipoExamen) object;
        if ((this.idTipoExamen == null && other.idTipoExamen != null) || (this.idTipoExamen != null && !this.idTipoExamen.equals(other.idTipoExamen))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues_occ_ingenieria_pp115_2026_salud.galenosv.entity.TipoExamen[ idTipoExamen=" + idTipoExamen + " ]";
    }
    
}
