/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues_occ_ingenieria_pp115_2026_salud.galenosv.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author mrxto
 */
@Entity
@Table(name = "persona_rol")
@NamedQueries({
    @NamedQuery(name = "PersonaRol.findAll", query = "SELECT p FROM PersonaRol p"),
    @NamedQuery(name = "PersonaRol.findByFechaCreacion", query = "SELECT p FROM PersonaRol p WHERE p.fechaCreacion = :fechaCreacion")})
public class PersonaRol implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    
    @Column(name = "id_persona_rol")
    private UUID idPersonaRol;
    @Column(name = "fecha_creacion")
    
    private OffsetDateTime fechaCreacion;
    @OneToMany(mappedBy = "idPersonaRol")
    private List<ConsultaProcedimientoPaso> consultaProcedimientoPasoList;
    @JoinColumn(name = "id_clinica", referencedColumnName = "id_clinica")
    @ManyToOne
    private Clinica idClinica;
    @JoinColumn(name = "id_persona", referencedColumnName = "id_persona")
    @ManyToOne
    private Persona idPersona;
    @JoinColumn(name = "id_rol", referencedColumnName = "id_rol")
    @ManyToOne
    private Rol idRol;
    @OneToMany(mappedBy = "idPersonaRol")
    private List<Consulta> consultaList;

    public PersonaRol() {
    }

    public PersonaRol(UUID idPersonaRol) {
        this.idPersonaRol = idPersonaRol;
    }

    public UUID getIdPersonaRol() {
        return idPersonaRol;
    }

    public void setIdPersonaRol(UUID idPersonaRol) {
        this.idPersonaRol = idPersonaRol;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(OffsetDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<ConsultaProcedimientoPaso> getConsultaProcedimientoPasoList() {
        return consultaProcedimientoPasoList;
    }

    public void setConsultaProcedimientoPasoList(List<ConsultaProcedimientoPaso> consultaProcedimientoPasoList) {
        this.consultaProcedimientoPasoList = consultaProcedimientoPasoList;
    }

    public Clinica getIdClinica() {
        return idClinica;
    }

    public void setIdClinica(Clinica idClinica) {
        this.idClinica = idClinica;
    }

    public Persona getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(Persona idPersona) {
        this.idPersona = idPersona;
    }

    public Rol getIdRol() {
        return idRol;
    }

    public void setIdRol(Rol idRol) {
        this.idRol = idRol;
    }

    public List<Consulta> getConsultaList() {
        return consultaList;
    }

    public void setConsultaList(List<Consulta> consultaList) {
        this.consultaList = consultaList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idPersonaRol != null ? idPersonaRol.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PersonaRol)) {
            return false;
        }
        PersonaRol other = (PersonaRol) object;
        if ((this.idPersonaRol == null && other.idPersonaRol != null) || (this.idPersonaRol != null && !this.idPersonaRol.equals(other.idPersonaRol))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "sv.edu.ues_occ_ingenieria_pp115_2026_salud.galenosv.entity.PersonaRol[ idPersonaRol=" + idPersonaRol + " ]";
    }
    
}
