/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.salud.galenosv.control.DefaultDAO;

/**
 *
 * @author david
 */
public class AbstractModelTest {

    public AbstractModelTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    private FacesContext facesContext;
    private DefaultDAO<RegistroPrueba> dao;
    private AbstractModelImpl instance;

    @BeforeEach
    public void setUp() {
        facesContext = mock(FacesContext.class);
        dao = mock(DefaultDAO.class);
        instance = new AbstractModelImpl();
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of getFacesContext method, of class AbstractModel.
     */
    @Test
    public void testGetFacesContext() {
        FacesContext resultado = instance.getFacesContext();

        assertSame(facesContext, resultado);
    }

    /**
     * Test of getDao method, of class AbstractModel.
     */
    @Test
    public void testGetDao() {
        DefaultDAO<RegistroPrueba> resultado = instance.getDao();

        assertSame(dao, resultado);
    }

    /**
     * Test of nuevoRegistro method, of class AbstractModel.
     */
    @Test
    public void testNuevoRegistro() {
        RegistroPrueba resultado = instance.nuevoRegistro();

        assertNotNull(resultado);
    }

    /**
     * Test of buscarRegistroPorId method, of class AbstractModel.
     */
    @Test
    public void testBuscarRegistroPorId() {
        RegistroPrueba resultado = instance.buscarRegistroPorId("10");

        assertNotNull(resultado);
        assertEquals("10", resultado.getId());
    }

    /**
     * Test of getIdAsText method, of class AbstractModel.
     */
    @Test
    public void testGetIdAsText() {
        RegistroPrueba registro = new RegistroPrueba("15", "Prueba");

        String resultado = instance.getIdAsText(registro);

        assertEquals("15", resultado);
    }

    /**
     * Test of getIdByText method, of class AbstractModel.
     */
    @Test
    public void testGetIdByText() {
        RegistroPrueba resultado = instance.getIdByText("20");

        assertNotNull(resultado);
        assertEquals("20", resultado.getId());
    }

    /**
     * Test of inicializar method, of class AbstractModel.
     */
    @Test
    public void testInicializar() {
        instance.inicializar();

        assertNotNull(instance.getModelo());
    }

    @Test
    public void testGetRowKey() {
        instance.inicializarRegistros();

        RegistroPrueba registro = new RegistroPrueba("10", "Prueba");

        String resultado = instance.getModelo().getRowKey(registro);

        assertEquals("10", resultado);
    }

    @Test
    public void testGetRowKeyNulo() {
        instance.inicializarRegistros();

        String resultado = instance.getModelo().getRowKey(null);

        assertNull(resultado);
    }

    /**
     * Test of inicializarRegistros method, of class AbstractModel.
     */
    @Test
    public void testInicializarRegistros() {
        instance.inicializarRegistros();

        assertNotNull(instance.getModelo());
    }

    @Test
    public void testGetRowKeyExcepcion() {
        AbstractModelImpl modeloError = new AbstractModelImpl() {
            @Override
            protected String getIdAsText(RegistroPrueba r) {
                throw new RuntimeException("Error de prueba");
            }
        };

        modeloError.inicializarRegistros();

        String resultado = modeloError.getModelo().getRowKey(
                new RegistroPrueba("10", "Prueba")
        );

        assertNull(resultado);
    }

    @Test
    public void testGetRowData() {
        instance.inicializarRegistros();

        RegistroPrueba resultado = instance.getModelo().getRowData("15");

        assertNotNull(resultado);
        assertEquals("15", resultado.getId());
    }

    @Test
    public void testGetRowDataNulo() {
        instance.inicializarRegistros();

        RegistroPrueba resultado = instance.getModelo().getRowData(null);

        assertNull(resultado);
    }

    @Test
    public void testGetRowDataExcepcion() {
        AbstractModelImpl modeloError = new AbstractModelImpl() {
            @Override
            protected RegistroPrueba getIdByText(String id) {
                throw new RuntimeException("Error de prueba");
            }
        };

        modeloError.inicializarRegistros();

        RegistroPrueba resultado = modeloError.getModelo().getRowData("10");

        assertNull(resultado);
    }

    //test count
    @Test
    public void testCount() {
        instance.inicializarRegistros();

        when(dao.count()).thenReturn(25L);

        int resultado = instance.getModelo().count(new HashMap<>());

        assertEquals(25, resultado);
        verify(dao).count();
    }

    @Test
    public void testCountExcepcion() {
        instance.inicializarRegistros();

        when(dao.count()).thenThrow(new RuntimeException("Error de prueba"));

        int resultado = instance.getModelo().count(new HashMap<>());

        assertEquals(0, resultado);
        verify(dao).count();
    }

// test load
    @Test
    public void testLoad() {
        instance.inicializarRegistros();

        List<RegistroPrueba> registros = List.of(
                new RegistroPrueba("1", "Uno"),
                new RegistroPrueba("2", "Dos")
        );

        when(dao.findRange(0, 10)).thenReturn(registros);

        List<RegistroPrueba> resultado = instance.getModelo().load(
                0, 10, new HashMap<>(), new HashMap<>()
        );

        assertEquals(registros, resultado);
        verify(dao).findRange(0, 10);
    }

    @Test
    public void testLoadExcepcion() {
        instance.inicializarRegistros();

        when(dao.findRange(0, 10))
                .thenThrow(new RuntimeException("Error de prueba"));

        List<RegistroPrueba> resultado = instance.getModelo().load(
                0, 10, new HashMap<>(), new HashMap<>()
        );

        assertTrue(resultado.isEmpty());
        verify(dao).findRange(0, 10);
    }

    /**
     * Test of getIdByRegistro method, of class AbstractModel.
     */
    @Test
    public void testGetIdByRegistro() {
        RegistroPrueba registro = new RegistroPrueba("10", "Producto");

        String resultado = instance.getIdByRegistro(registro);

        assertEquals("10", resultado);
    }

    /**
     * Test of getRegistroById method, of class AbstractModel.
     */
    @Test
    public void testGetRegistroById() {
        RegistroPrueba esperado = new RegistroPrueba("20", "Registro");

        RegistroPrueba resultado = instance.getRegistroById("20");

        assertNotNull(resultado);
        assertEquals(esperado.getId(), resultado.getId());
        assertEquals(esperado.getNombre(), resultado.getNombre());
    }

    /**
     * Test of selectionHandler method, of class AbstractModel.
     */
    @Test
    public void testSelectionHandler() {
        RegistroPrueba registro = new RegistroPrueba("1", "Prueba");

        SelectEvent<RegistroPrueba> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(registro);

        instance.selectionHandler(evento);

        assertSame(registro, instance.getRegistro());
        assertEquals(ESTADO_CRUD.MODIFICAR, instance.getEstado());
    }

    @Test
    public void testSelectionHandlerEventoNulo() {
        instance.selectionHandler(null);

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    @Test
    public void testSelectionHandlerObjetoNulo() {
        SelectEvent<RegistroPrueba> evento = mock(SelectEvent.class);
        when(evento.getObject()).thenReturn(null);

        instance.selectionHandler(evento);

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    /**
     * Test of btnNuevoHandler method, of class AbstractModel.
     */
    @Test
    public void testBtnNuevoHandler() {
        instance.btnNuevoHandler(null);

        assertNotNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.CREAR, instance.getEstado());
    }

    /**
     * Test of btnCancelarHandler method, of class AbstractModel.
     */
    @Test
    public void testBtnCancelarHandler() {
        RegistroPrueba registro = new RegistroPrueba("1", "Prueba");

        instance.setRegistro(registro);
        instance.setEstado(ESTADO_CRUD.MODIFICAR);

        instance.btnCancelarHandler(null);

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
    }

    /**
     * Test of btnGuardarHandler method, of class AbstractModel.
     */
    @Test
    public void testBtnGuardarHandlerRegistroNulo() {
        instance.setRegistro(null);

        instance.btnGuardarHandler(null);

        assertNull(instance.getRegistro());
        verify(dao, never()).crear(any());
        verify(facesContext, never()).addMessage(any(), any());
    }

    @Test
    public void testBtnGuardarHandlerNombreVacio() {
        RegistroPrueba registro = new RegistroPrueba("1", "   ");
        instance.setRegistro(registro);

        instance.btnGuardarHandler(null);

        assertSame(registro, instance.getRegistro());

        verify(dao, never()).crear(any());
        verify(facesContext).addMessage(
                isNull(),
                argThat(mensaje
                        -> mensaje.getSeverity() == FacesMessage.SEVERITY_WARN
                )
        );
    }

    @Test
    public void testBtnGuardarHandlerExitoso() {
        RegistroPrueba registro = new RegistroPrueba("1", "Prueba");
        instance.setRegistro(registro);

        instance.btnGuardarHandler(null);

        verify(dao).crear(registro);

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNotNull(instance.getModelo());

        verify(facesContext).addMessage(
                isNull(),
                argThat(mensaje
                        -> mensaje.getSeverity() == FacesMessage.SEVERITY_INFO
                )
        );
    }

    @Test
    public void testBtnGuardarHandlerErrorUnicidad() {
        AbstractModelImpl modeloError = new AbstractModelImpl() {
            @Override
            protected void validarUnicidad(
                    RegistroPrueba registro,
                    boolean esModificacion) {
                throw new IllegalArgumentException("Registro duplicado");
            }
        };

        RegistroPrueba registro = new RegistroPrueba("1", "Prueba");
        modeloError.setRegistro(registro);

        modeloError.btnGuardarHandler(null);

        assertSame(registro, modeloError.getRegistro());

        verify(dao, never()).crear(any());

        verify(facesContext).addMessage(
                isNull(),
                argThat(mensaje
                        -> mensaje.getSeverity() == FacesMessage.SEVERITY_ERROR
                )
        );
    }

    @Test
    public void testBtnGuardarHandlerErrorDAO() {
        RegistroPrueba registro = new RegistroPrueba("1", "Prueba");
        instance.setRegistro(registro);

        doThrow(new RuntimeException("Error de base de datos"))
                .when(dao).crear(registro);

        instance.btnGuardarHandler(null);

        assertSame(registro, instance.getRegistro());

        verify(dao).crear(registro);

        verify(facesContext).addMessage(
                isNull(),
                argThat(mensaje
                        -> mensaje.getSeverity() == FacesMessage.SEVERITY_ERROR
                )
        );
    }

    /**
     * Test of btnEliminarHandler method, of class AbstractModel.
     */
    @Test
    public void testBtnEliminarHandlerRegistroNulo() {
        instance.setRegistro(null);

        instance.btnEliminarHandler(null);

        assertNull(instance.getRegistro());

        verify(dao, never()).eliminar(any());
        verify(facesContext, never()).addMessage(any(), any());
    }

    @Test
    public void testBtnEliminarHandlerExitoso() {
        RegistroPrueba registro = new RegistroPrueba("1", "Prueba");
        instance.setRegistro(registro);

        instance.btnEliminarHandler(null);

        verify(dao).eliminar(registro);

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNotNull(instance.getModelo());

        verify(facesContext).addMessage(
                isNull(),
                argThat(mensaje
                        -> mensaje.getSeverity() == FacesMessage.SEVERITY_INFO
                )
        );
    }

    @Test
    public void testBtnEliminarHandlerErrorValidacion() {
        AbstractModelImpl modeloError = new AbstractModelImpl() {
            @Override
            protected void validarEliminacion(RegistroPrueba registro) {
                throw new IllegalArgumentException("Tiene registros relacionados");
            }
        };

        RegistroPrueba registro = new RegistroPrueba("1", "Prueba");
        modeloError.setRegistro(registro);

        modeloError.btnEliminarHandler(null);

        assertSame(registro, modeloError.getRegistro());

        verify(dao, never()).eliminar(any());

        verify(facesContext).addMessage(
                isNull(),
                argThat(mensaje
                        -> mensaje.getSeverity() == FacesMessage.SEVERITY_ERROR
                )
        );
    }

    @Test
    public void testBtnEliminarHandlerErrorDAO() {
        RegistroPrueba registro = new RegistroPrueba("1", "Prueba");
        instance.setRegistro(registro);

        doThrow(new RuntimeException("Error de base de datos"))
                .when(dao).eliminar(registro);

        instance.btnEliminarHandler(null);

        assertSame(registro, instance.getRegistro());

        verify(dao).eliminar(registro);

        verify(facesContext).addMessage(
                isNull(),
                argThat(mensaje
                        -> mensaje.getSeverity() == FacesMessage.SEVERITY_ERROR
                )
        );
    }

    /**
     * Test of btnModificarHandler method, of class AbstractModel.
     */
    @Test
    public void testBtnModificarHandlerRegistroNulo() {
        instance.setRegistro(null);

        instance.btnModificarHandler(null);

        assertNull(instance.getRegistro());

        verify(dao, never()).modificar(any());
        verify(facesContext, never()).addMessage(any(), any());
    }

    @Test
    public void testBtnModificarHandlerExitoso() {
        RegistroPrueba registro = new RegistroPrueba("1", "Prueba");
        instance.setRegistro(registro);

        when(dao.modificar(registro)).thenReturn(registro);

        instance.btnModificarHandler(null);

        verify(dao).modificar(registro);

        assertNull(instance.getRegistro());
        assertEquals(ESTADO_CRUD.NADA, instance.getEstado());
        assertNotNull(instance.getModelo());

        verify(facesContext).addMessage(
                isNull(),
                argThat(mensaje
                        -> mensaje.getSeverity() == FacesMessage.SEVERITY_INFO
                )
        );
    }

    @Test
    public void testBtnModificarHandlerErrorUnicidad() {
        AbstractModelImpl modeloError = new AbstractModelImpl() {
            @Override
            protected void validarUnicidad(
                    RegistroPrueba registro,
                    boolean esModificacion) {
                assertTrue(esModificacion);
                throw new IllegalArgumentException("Registro duplicado");
            }
        };

        RegistroPrueba registro = new RegistroPrueba("1", "Prueba");
        modeloError.setRegistro(registro);

        modeloError.btnModificarHandler(null);

        assertSame(registro, modeloError.getRegistro());

        verify(dao, never()).modificar(any());

        verify(facesContext).addMessage(
                isNull(),
                argThat(mensaje
                        -> mensaje.getSeverity() == FacesMessage.SEVERITY_ERROR
                )
        );
    }

    @Test
    public void testBtnModificarHandlerErrorDAO() {
        RegistroPrueba registro = new RegistroPrueba("1", "Prueba");
        instance.setRegistro(registro);

        when(dao.modificar(registro))
                .thenThrow(new RuntimeException("Error de base de datos"));

        instance.btnModificarHandler(null);

        assertSame(registro, instance.getRegistro());

        verify(dao).modificar(registro);

        verify(facesContext).addMessage(
                isNull(),
                argThat(mensaje
                        -> mensaje.getSeverity() == FacesMessage.SEVERITY_ERROR
                )
        );
    }

    @Test
    public void testBtnModificarHandlerNombreVacio() {
        RegistroPrueba registro = new RegistroPrueba("1", "   ");
        instance.setRegistro(registro);

        instance.btnModificarHandler(null);

        assertSame(registro, instance.getRegistro());

        verify(dao, never()).modificar(any());

        verify(facesContext).addMessage(
                isNull(),
                argThat(mensaje
                        -> mensaje.getSeverity() == FacesMessage.SEVERITY_WARN
                )
        );
    }

    /**
     * Test of validarUnicidad method, of class AbstractModel.
     */
    @Test
    public void testValidarUnicidad() {
        RegistroPrueba registro = new RegistroPrueba("1", "Producto");
        boolean esModificacion = false;

        AbstractModel<RegistroPrueba> instance = new AbstractModelImpl();

        assertDoesNotThrow(()
                -> instance.validarUnicidad(registro, esModificacion)
        );
    }

    /**
     * Test of validarEliminacion method, of class AbstractModel.
     */
    @Test
    public void testValidarEliminacion() {
        RegistroPrueba registro = new RegistroPrueba("1", "Producto");

        AbstractModel<RegistroPrueba> instance = new AbstractModelImpl();

        assertDoesNotThrow(()
                -> instance.validarEliminacion(registro)
        );
    }

    /**
     * Test of esNombreVacio method, of class AbstractModel.
     */
    @Test
    public void testEsNombreVacioRegistroNulo() {
        boolean resultado = instance.esNombreVacio(null);

        assertTrue(resultado);
    }

    @Test
    public void testEsNombreVacioNombreNulo() {
        RegistroPrueba registro = new RegistroPrueba("1", null);

        boolean resultado = instance.esNombreVacio(registro);

        assertTrue(resultado);
    }

    @Test
    public void testEsNombreVacioNombreVacio() {
        RegistroPrueba registro = new RegistroPrueba("1", "");

        boolean resultado = instance.esNombreVacio(registro);

        assertTrue(resultado);
    }

    @Test
    public void testEsNombreVacioSoloEspacios() {
        RegistroPrueba registro = new RegistroPrueba("1", "     ");

        boolean resultado = instance.esNombreVacio(registro);

        assertTrue(resultado);
    }

    @Test
    public void testEsNombreVacioNombreValido() {
        RegistroPrueba registro = new RegistroPrueba("1", "Producto");

        boolean resultado = instance.esNombreVacio(registro);

        assertFalse(resultado);
    }

    @Test
    public void testEsNombreVacioSinGetNombre() {
        AbstractModelObject modelo = new AbstractModelObject();

        boolean resultado = modelo.esNombreVacio(new Object());

        assertFalse(resultado);
    }

    @Test
    public void testEsNombreVacioErrorReflexion() {
        AbstractModelObject modelo = new AbstractModelObject();

        RegistroIncorrecto registro = new RegistroIncorrecto();

        assertThrows(
                IllegalStateException.class,
                () -> modelo.esNombreVacio(registro)
        );
    }

    /**
     * Test of getEstado method, of class AbstractModel.
     */
    @Test
    public void testGetEstado() {
        instance.setEstado(ESTADO_CRUD.CREAR);

        assertEquals(ESTADO_CRUD.CREAR, instance.getEstado());
    }

    /**
     * Test of setEstado method, of class AbstractModel.
     */
    @Test
    public void testSetEstado() {
        instance.setEstado(ESTADO_CRUD.MODIFICAR);

        assertEquals(ESTADO_CRUD.MODIFICAR, instance.getEstado());
    }

    /**
     * Test of getNombreBean method, of class AbstractModel.
     */
    @Test
    public void testGetNombreBean() {
        instance.setNombreBean("productoBean");

        assertEquals("productoBean", instance.getNombreBean());
    }

    /**
     * Test of setNombreBean method, of class AbstractModel.
     */
    @Test
    public void testSetNombreBean() {
        instance.setNombreBean("usuarioBean");

        assertEquals("usuarioBean", instance.getNombreBean());
    }

    /**
     * Test of getRegistro method, of class AbstractModel.
     */
    @Test
    public void testGetRegistro() {
        RegistroPrueba registro = new RegistroPrueba("1", "Producto");

        instance.setRegistro(registro);

        assertSame(registro, instance.getRegistro());
    }

    /**
     * Test of setRegistro method, of class AbstractModel.
     */
    @Test
    public void testSetRegistro() {
        RegistroPrueba registro = new RegistroPrueba("2", "Usuario");

        instance.setRegistro(registro);

        assertSame(registro, instance.getRegistro());
    }

    /**
     * Test of getModelo method, of class AbstractModel.
     */
    @Test
    public void testGetModelo() {
        LazyDataModel<RegistroPrueba> modelo = mock(LazyDataModel.class);

        instance.setModelo(modelo);

        assertSame(modelo, instance.getModelo());
    }

    /**
     * Test of setModelo method, of class AbstractModel.
     */
    @Test
    public void testSetModelo() {
        LazyDataModel<RegistroPrueba> modelo = mock(LazyDataModel.class);

        instance.setModelo(modelo);

        assertSame(modelo, instance.getModelo());
    }

    /**
     * Test of getCantidadRegistros method, of class AbstractModel.
     */
    @Test
    public void testGetCantidadRegistros() {
        instance.setCantidadRegistros(100);

        assertEquals(100, instance.getCantidadRegistros());
    }

    /**
     * Test of setCantidadRegistros method, of class AbstractModel.
     */
    @Test
    public void testSetCantidadRegistros() {
        instance.setCantidadRegistros(200);

        assertEquals(200, instance.getCantidadRegistros());
    }

    @Test
    public void testCantidadRegistrosInicial() {
        assertEquals(50, instance.getCantidadRegistros());
    }

    public class AbstractModelImpl extends AbstractModel<RegistroPrueba> {

        @Override
        protected FacesContext getFacesContext() {
            return facesContext;
        }

        @Override
        protected DefaultDAO<RegistroPrueba> getDao() {
            return dao;
        }

        @Override
        protected RegistroPrueba nuevoRegistro() {
            return new RegistroPrueba();
        }

        @Override
        protected RegistroPrueba buscarRegistroPorId(Object id) {
            return new RegistroPrueba(id.toString(), "Registro");
        }

        @Override
        protected String getIdAsText(RegistroPrueba r) {
            return r.getId();
        }

        @Override
        protected RegistroPrueba getIdByText(String id) {
            return new RegistroPrueba(id, "Registro");
        }
    }

    public static class RegistroPrueba {

        private String id;
        private String nombre;

        public RegistroPrueba() {
        }

        public RegistroPrueba(String id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public String getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }
    }

    public static class RegistroIncorrecto {

        public Integer getNombre() {
            return 123;
        }
    }

    public class AbstractModelObject extends AbstractModel<Object> {

        @Override
        protected FacesContext getFacesContext() {
            return facesContext;
        }

        @Override
        protected DefaultDAO<Object> getDao() {
            return mock(DefaultDAO.class);
        }

        @Override
        protected Object nuevoRegistro() {
            return new Object();
        }

        @Override
        protected Object buscarRegistroPorId(Object id) {
            return id;
        }

        @Override
        protected String getIdAsText(Object r) {
            return null;
        }

        @Override
        protected Object getIdByText(String id) {
            return null;
        }
    }
}
