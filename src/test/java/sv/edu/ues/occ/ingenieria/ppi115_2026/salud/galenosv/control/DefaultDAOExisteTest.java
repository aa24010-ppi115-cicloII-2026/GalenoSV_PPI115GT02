package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DefaultDAOExisteTest {

    @Test
    void existeTextoRecortaElValorYConservaLaComparacionSinDistinguirMayusculas() {
        EntityManager em = mock(EntityManager.class);
        TypedQuery<Long> consulta = consultaConConteo(em, 2L);

        boolean resultado = new Dao(em).existeTexto("nombre", "id", "nom", "  Clinica Uno  ", null);

        assertTrue(resultado);
        assertEquals("SELECT COUNT(e) FROM String e WHERE LOWER(e.nombre) = LOWER(:nom)", jpql(em));
        verify(consulta).setParameter("nom", "Clinica Uno");
        verify(consulta, never()).setParameter(eq("excluir"), any());
    }

    @Test
    void existeTextoConExcluirAgregaLaCondicionYElParametro() {
        EntityManager em = mock(EntityManager.class);
        TypedQuery<Long> consulta = consultaConConteo(em, 0L);
        UUID excluir = UUID.randomUUID();

        boolean resultado = new Dao(em).existeTexto("nombre", "id", "nom", "Clinica", excluir);

        assertFalse(resultado);
        assertEquals("SELECT COUNT(e) FROM String e WHERE LOWER(e.nombre) = LOWER(:nom)"
                + " AND e.id <> :excluir", jpql(em));
        verify(consulta).setParameter("nom", "Clinica");
        verify(consulta).setParameter("excluir", excluir);
    }

    @Test
    void existeTextoConValorAusenteNoAbreConsulta() {
        EntityManager em = mock(EntityManager.class);
        Dao dao = new Dao(em);

        assertFalse(dao.existeTexto("nombre", "id", "nom", null, null));
        assertFalse(dao.existeTexto("nombre", "id", "nom", "   ", null));

        verifyNoInteractions(em);
    }

    @Test
    void existeTextoIgualMantieneLaComparacionExactaDelIdentificador() {
        EntityManager em = mock(EntityManager.class);
        TypedQuery<Long> consulta = consultaConConteo(em, 3L);

        boolean resultado = new Dao(em).existeTextoIgual("d", "valor", "idDocumento", "val", " 001 ", null);

        assertTrue(resultado);
        assertEquals("SELECT COUNT(d) FROM String d WHERE d.valor = :val", jpql(em));
        verify(consulta).setParameter("val", "001");
    }

    @Test
    void existeConFiltrosConservaElOrdenDeLasCondicionesYDeLosParametros() {
        EntityManager em = mock(EntityManager.class);
        TypedQuery<Long> consulta = consultaConConteo(em, 1L);
        UUID persona = UUID.randomUUID();
        UUID rol = UUID.randomUUID();
        UUID clinica = UUID.randomUUID();
        UUID excluir = UUID.randomUUID();

        boolean resultado = new Dao(em).existeConFiltros("p", "idPersonaRol", excluir,
                new DefaultDAO.Filtro("idPersona.idPersona", "per", persona),
                new DefaultDAO.Filtro("idRol.idRol", "rol", rol),
                new DefaultDAO.Filtro("idClinica.idClinica", "cli", clinica));

        assertTrue(resultado);
        assertEquals("SELECT COUNT(p) FROM String p WHERE p.idPersona.idPersona = :per"
                + " AND p.idRol.idRol = :rol AND p.idClinica.idClinica = :cli"
                + " AND p.idPersonaRol <> :excluir", jpql(em));
        InOrder orden = inOrder(consulta);
        orden.verify(consulta).setParameter("per", persona);
        orden.verify(consulta).setParameter("rol", rol);
        orden.verify(consulta).setParameter("cli", clinica);
        orden.verify(consulta).setParameter("excluir", excluir);
    }

    @Test
    void existePersonaValorConservaLaUnicidadPorPersonaYNoPorValorGlobal() {
        EntityManager em = mock(EntityManager.class);
        TypedQuery<Long> consulta = consultaConConteo(em, 1L);
        UUID persona = UUID.randomUUID();

        boolean resultado = new MedioContactoDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        }.existePersonaValor(persona, " correo@dominio.com ", null);

        assertTrue(resultado);
        assertEquals("SELECT COUNT(m) FROM MedioContacto m WHERE m.idPersona.idPersona = :per"
                + " AND m.valor = :val", jpql(em));
        verify(consulta).setParameter("per", persona);
        verify(consulta).setParameter("val", "correo@dominio.com");
    }

    @Test
    void existeVinculoConservaLaClaveCompuestaPorExamenYTipo() {
        EntityManager em = mock(EntityManager.class);
        TypedQuery<Long> consulta = consultaConConteo(em, 1L);
        UUID examen = UUID.randomUUID();
        UUID tipo = UUID.randomUUID();

        boolean resultado = new ExamenTipoExamenDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        }.existeVinculo(examen, tipo, null);

        assertTrue(resultado);
        assertEquals("SELECT COUNT(e) FROM ExamenTipoExamen e WHERE e.idExamen.idExamen = :exa"
                + " AND e.idTipoExamen.idTipoExamen = :tip", jpql(em));
        verify(consulta).setParameter("exa", examen);
        verify(consulta).setParameter("tip", tipo);
    }

    @Test
    void existeAsignacionConservaLaClinicaComoParteDeLaClave() {
        EntityManager em = mock(EntityManager.class);
        TypedQuery<Long> consulta = consultaConConteo(em, 1L);
        UUID persona = UUID.randomUUID();
        UUID rol = UUID.randomUUID();
        UUID clinica = UUID.randomUUID();
        UUID excluir = UUID.randomUUID();

        boolean resultado = new PersonaRolDAO() {
            @Override
            public EntityManager getEntityManager() {
                return em;
            }
        }.existeAsignacion(persona, rol, clinica, excluir);

        assertTrue(resultado);
        assertEquals("SELECT COUNT(p) FROM PersonaRol p WHERE p.idPersona.idPersona = :per"
                + " AND p.idRol.idRol = :rol AND p.idClinica.idClinica = :cli"
                + " AND p.idPersonaRol <> :excluir", jpql(em));
        verify(consulta).setParameter("per", persona);
        verify(consulta).setParameter("rol", rol);
        verify(consulta).setParameter("cli", clinica);
        verify(consulta).setParameter("excluir", excluir);
    }

    @Test
    void eliminarTraduceLaLlaveForaneaAunqueVengaEnUnaCausaAnidada() {
        EntityManager em = mock(EntityManager.class);
        String registro = "paciente";
        when(em.contains(registro)).thenReturn(true);
        doThrow(new RuntimeException("commit", new RuntimeException("ERROR: FK_CONSULTA"))).when(em).flush();

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> new Dao(em).eliminar(registro));

        assertEquals("No se puede eliminar porque tiene registros relacionados", error.getMessage());
        assertInstanceOf(RuntimeException.class, error.getCause());
        verify(em).remove(registro);
    }

    private static TypedQuery<Long> consultaConConteo(EntityManager em, long conteo) {
        @SuppressWarnings("unchecked")
        TypedQuery<Long> consulta = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(consulta);
        when(consulta.getSingleResult()).thenReturn(conteo);
        return consulta;
    }

    private static String jpql(EntityManager em) {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(em).createQuery(captor.capture(), eq(Long.class));
        return captor.getValue();
    }

    private static class Dao extends DefaultDAO<String> {

        private final EntityManager em;

        Dao(EntityManager em) {
            super(String.class);
            this.em = em;
        }

        @Override
        public EntityManager getEntityManager() {
            return em;
        }
    }
}
