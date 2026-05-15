import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.text.Normalizer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.UIManager;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

class Estudiante {
  private static final int[] MESES_CORTE_1 = new int[] { 2, 3, 4 };
  private static final int[] MESES_CORTE_2 = new int[] { 5, 6, 7 };
  private static final int[] MESES_CORTE_3 = new int[] { 8, 9, 10, 11 };

  private final int id;
  private String nombre;
  private int edad;
  private String grado;
  private int curso;
  private Map<Integer, List<Double>> notasPorCorte;
  private Map<String, Map<Integer, List<Double>>> notasPorMateria;
  private Map<Integer, Integer> faltasPorMes;
  private int totalFaltas;
  private String nivelRiesgo;
  private String nombreAcudiente;
  private String correoAcudiente;
  private String telefonoAcudiente;
  private int mesRegistro;

  public Estudiante(int id, String nombre, int edad, String grado, int curso, String nombreAcudiente,
      String correoAcudiente, String telefonoAcudiente) {
    this.id = id;
    this.nombre = nombre;
    this.edad = edad;
    this.grado = grado;
    this.curso = curso;
    this.nombreAcudiente = nombreAcudiente;
    this.correoAcudiente = correoAcudiente;
    this.telefonoAcudiente = telefonoAcudiente;
    this.notasPorCorte = new HashMap<>();
    this.notasPorMateria = new HashMap<>();
    this.faltasPorMes = new HashMap<>();
    this.totalFaltas = 0;
    this.nivelRiesgo = "NORMAL";
    this.mesRegistro = java.time.LocalDate.now().getMonthValue();
  }

  public Estudiante(int id, String nombre, int edad, String grado, int curso, Map<Integer, List<Double>> notas,
      Map<String, Map<Integer, List<Double>>> notasPorMateria, Map<Integer, Integer> faltasPorMes, int totalFaltas,
      String nombreAcudiente,
      String correoAcudiente, String telefonoAcudiente, int mesRegistro) {
    this.id = id;
    this.nombre = nombre;
    this.edad = edad;
    this.grado = grado;
    this.curso = curso;
    this.notasPorCorte = new HashMap<>(notas);
    this.notasPorMateria = new HashMap<>();
    if (notasPorMateria != null) {
      for (Map.Entry<String, Map<Integer, List<Double>>> entry : notasPorMateria.entrySet()) {
        Map<Integer, List<Double>> cortes = new HashMap<>();
        for (Map.Entry<Integer, List<Double>> c : entry.getValue().entrySet()) {
          cortes.put(c.getKey(), new ArrayList<>(c.getValue()));
        }
        this.notasPorMateria.put(entry.getKey(), cortes);
      }
    }
    this.faltasPorMes = new HashMap<>();
    if (faltasPorMes != null) {
      this.faltasPorMes.putAll(faltasPorMes);
    }
    this.totalFaltas = totalFaltas;
    this.nombreAcudiente = nombreAcudiente;
    this.correoAcudiente = correoAcudiente;
    this.telefonoAcudiente = telefonoAcudiente;
    this.mesRegistro = mesRegistro;
    actualizarRiesgo();
  }

  public void agregarNotas(int corte, List<Double> nuevasNotas) {
    this.notasPorCorte.putIfAbsent(corte, new ArrayList<>());
    this.notasPorCorte.get(corte).addAll(nuevasNotas);
    actualizarRiesgo();
  }

  public void agregarNotasMateria(String materia, int corte, List<Double> nuevasNotas) {
    if (materia == null || materia.trim().isEmpty()) {
      materia = "General";
    }
    String m = materia.trim();
    this.notasPorMateria.putIfAbsent(m, new HashMap<>());
    this.notasPorMateria.get(m).putIfAbsent(corte, new ArrayList<>());
    this.notasPorMateria.get(m).get(corte).addAll(nuevasNotas);
    agregarNotas(corte, nuevasNotas);
  }

  public void editarNota(int corte, int indice, double nuevaNota) {
    if (notasPorCorte.containsKey(corte) && indice < notasPorCorte.get(corte).size()) {
      notasPorCorte.get(corte).set(indice, nuevaNota);
      actualizarRiesgo();
    }
  }

  public void agregarFaltas(int faltasNuevas) {
    this.totalFaltas += faltasNuevas;
    actualizarRiesgo();
  }

  public void agregarFaltasMes(int mes, int faltasNuevas) {
    if (mes < 1 || mes > 12) {
      return;
    }
    if (faltasNuevas <= 0) {
      return;
    }
    this.faltasPorMes.put(mes, this.faltasPorMes.getOrDefault(mes, 0) + faltasNuevas);
    this.totalFaltas += faltasNuevas;
    actualizarRiesgo();
  }

  public int getFaltasMes(int mes) {
    return faltasPorMes.getOrDefault(mes, 0);
  }

  public void actualizarRiesgo() {
    int materiasPerdidas = contarMateriasPerdidas();
    int estadoInasistencia = peorEstadoInasistencias();

    if (materiasPerdidas >= 3 && (estadoInasistencia >= 2 || totalFaltas >= 8)) {
      this.nivelRiesgo = "RIESGO DE DESERCIÓN";
      return;
    }
    if (materiasPerdidas >= 1 && (estadoInasistencia >= 1 || totalFaltas >= 2)) {
      this.nivelRiesgo = "ALERTA DE DESERCIÓN";
      return;
    }

    if (materiasPerdidas >= 1) {
      this.nivelRiesgo = "ALERTA ACADÉMICA";
      return;
    }

    if (estadoInasistencia >= 1 || totalFaltas >= 5) {
      this.nivelRiesgo = "ALERTA DE ASISTENCIA";
      return;
    }

    this.nivelRiesgo = "NORMAL";
  }

  private int contarMateriasPerdidas() {
    java.util.Set<String> materiasConFalla = new java.util.HashSet<>();
    if (notasPorMateria != null && !notasPorMateria.isEmpty()) {
      for (String materia : oficial.MATERIAS_BASICAS) {
        Double promG = promedioMateria(materia);
        if (promG != null && promG < 3.0) {
          materiasConFalla.add(materia);
        }
        for (int i = 1; i <= 3; i++) {
          Double promC = promedioMateriaCorte(materia, i);
          if (promC != null && promC < 3.0) {
            materiasConFalla.add(materia);
          }
        }
      }
    } else if (notasPorCorte != null && !notasPorCorte.isEmpty()) {
      for (int i = 1; i <= 3; i++) {
        double prom = getPromedioCorte(i);
        if (prom > 0 && prom < 3.0) {
          materiasConFalla.add("Corte " + i);
        }
      }
    }
    return materiasConFalla.size();
  }

  public int contarMateriasPerdidasCorte(int corte) {
    if (notasPorMateria == null || notasPorMateria.isEmpty()) {
      return 0;
    }
    int perdidas = 0;
    for (String materia : oficial.MATERIAS_BASICAS) {
      Double prom = promedioMateriaCorte(materia, corte);
      if (prom != null && prom < 3.0) {
        perdidas++;
      }
    }
    return perdidas;
  }

  private Double promedioMateriaCorte(String materia, int corte) {
    if (materia == null || materia.trim().isEmpty()) {
      return null;
    }
    String objetivo = normalizarTexto(materia);
    List<Double> notas = new ArrayList<>();
    for (Map.Entry<String, Map<Integer, List<Double>>> e : notasPorMateria.entrySet()) {
      if (e.getKey() == null)
        continue;
      if (!normalizarTexto(e.getKey()).equals(objetivo))
        continue;
      if (e.getValue() == null)
        continue;
      List<Double> notasCorte = e.getValue().get(corte);
      if (notasCorte != null) {
        notas.addAll(notasCorte);
      }
    }
    if (notas.isEmpty()) {
      return null;
    }
    return notas.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
  }

  private Double promedioMateria(String materia) {
    if (materia == null || materia.trim().isEmpty()) {
      return null;
    }
    String objetivo = normalizarTexto(materia);
    List<Double> notas = new ArrayList<>();
    for (Map.Entry<String, Map<Integer, List<Double>>> e : notasPorMateria.entrySet()) {
      if (e.getKey() == null)
        continue;
      if (!normalizarTexto(e.getKey()).equals(objetivo))
        continue;
      if (e.getValue() == null)
        continue;
      e.getValue().values().stream().flatMap(List::stream).forEach(notas::add);
    }
    if (notas.isEmpty()) {
      return null;
    }
    return notas.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
  }

  private int peorEstadoInasistencias() {
    int peor = 0;
    peor = Math.max(peor, peorEstadoMensual());
    peor = Math.max(peor, peorEstadoPorCorte());
    peor = Math.max(peor, estadoPorAcumuladoSimple(totalFaltas, 1));
    return peor;
  }

  private int peorEstadoMensual() {
    if (faltasPorMes == null || faltasPorMes.isEmpty()) {
      return 0;
    }
    int peor = 0;
    for (int mes : MESES_CORTE_1) {
      peor = Math.max(peor, estadoMensual(getFaltasMes(mes)));
    }
    for (int mes : MESES_CORTE_2) {
      peor = Math.max(peor, estadoMensual(getFaltasMes(mes)));
    }
    for (int mes : MESES_CORTE_3) {
      peor = Math.max(peor, estadoMensual(getFaltasMes(mes)));
    }
    return peor;
  }

  private int peorEstadoPorCorte() {
    if (faltasPorMes == null || faltasPorMes.isEmpty()) {
      return 0;
    }
    int c1 = sumarMeses(MESES_CORTE_1);
    int c2 = sumarMeses(MESES_CORTE_2);
    int c3 = sumarMeses(MESES_CORTE_3);
    int peor = 0;
    peor = Math.max(peor, estadoPorAcumuladoSimple(c1, 3));
    peor = Math.max(peor, estadoPorAcumuladoSimple(c2, 3));
    peor = Math.max(peor, estadoPorAcumuladoSimple(c3, 4));
    return peor;
  }

  private int sumarMeses(int[] meses) {
    int sum = 0;
    for (int m : meses) {
      sum += getFaltasMes(m);
    }
    return sum;
  }

  private int estadoMensual(int faltas) {
    if (faltas >= 6)
      return 2;
    if (faltas >= 3)
      return 1;
    return 0;
  }

  private int estadoPorAcumuladoSimple(int faltas, int mesesCorte) {
    if (mesesCorte == 4) {
      if (faltas >= 21)
        return 2;
      if (faltas >= 9)
        return 1;
      return 0;
    }
    if (faltas >= 16)
      return 2;
    if (faltas >= 7)
      return 1;
    return 0;
  }

  static String normalizarTexto(String s) {
    if (s == null)
      return "";
    String t = Normalizer.normalize(s, Normalizer.Form.NFD);
    t = t.replaceAll("\\p{M}", "");
    return t.trim().toLowerCase();
  }

  public int getId() {
    return id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getGrado() {
    return grado;
  }

  public void setGrado(String grado) {
    this.grado = grado;
  }

  public int getCurso() {
    return curso;
  }

  public void setCurso(int curso) {
    this.curso = curso;
  }

  public Map<Integer, List<Double>> getNotasPorCorte() {
    return notasPorCorte;
  }

  public Map<String, Map<Integer, List<Double>>> getNotasPorMateria() {
    return notasPorMateria;
  }

  public int getTotalFaltas() {
    return totalFaltas;
  }

  public String getNivelRiesgo() {
    return nivelRiesgo;
  }

  public String getNombreAcudiente() {
    return nombreAcudiente;
  }

  public void setNombreAcudiente(String nombreAcudiente) {
    this.nombreAcudiente = nombreAcudiente;
  }

  public String getCorreoAcudiente() {
    return correoAcudiente;
  }

  public void setCorreoAcudiente(String correoAcudiente) {
    this.correoAcudiente = correoAcudiente;
  }

  public String getTelefonoAcudiente() {
    return telefonoAcudiente;
  }

  public void setTelefonoAcudiente(String telefonoAcudiente) {
    this.telefonoAcudiente = telefonoAcudiente;
  }

  public double getPromedioGeneral() {
    List<Double> todasLasNotas;
    if (notasPorMateria != null && !notasPorMateria.isEmpty()) {
      todasLasNotas = notasPorMateria.values().stream()
          .flatMap(m -> m.values().stream())
          .flatMap(List::stream)
          .collect(Collectors.toList());
    } else {
      todasLasNotas = notasPorCorte.values().stream()
          .flatMap(List::stream)
          .collect(Collectors.toList());
    }
    if (todasLasNotas.isEmpty())
      return 0.0;
    return todasLasNotas.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
  }

  public double getPromedioCorte(int corte) {
    List<Double> notas = new ArrayList<>();
    if (notasPorMateria != null && !notasPorMateria.isEmpty()) {
      for (Map<Integer, List<Double>> m : notasPorMateria.values()) {
        List<Double> nCorte = m.get(corte);
        if (nCorte != null)
          notas.addAll(nCorte);
      }
    } else {
      List<Double> nCorte = notasPorCorte.get(corte);
      if (nCorte != null)
        notas.addAll(nCorte);
    }
    if (notas.isEmpty())
      return 0.0;
    return notas.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
  }

  public static String desempeno(double nota) {
    if (nota >= 4.6 && nota <= 5.0)
      return "Superior";
    if (nota >= 4.0 && nota <= 4.5)
      return "Alto";
    if (nota >= 3.0 && nota <= 3.9)
      return "Básico";
    if (nota >= 1.0 && nota <= 2.9)
      return "Bajo";
    return "Sin datos";
  }

  @Override
  public String toString() {
    return String.format("ID: %d | %-15s | Grado: %s-%d | Prom: %.2f | Faltas: %d | Riesgo: %s | Acudiente: %s",
        id, nombre, grado, curso, getPromedioGeneral(), totalFaltas, nivelRiesgo, nombreAcudiente);
  }

  public int getMesRegistro() {
    return mesRegistro;
  }

  public void setMesRegistro(int mes) {
    this.mesRegistro = mes;
  }

  public String toDataString() {
    StringBuilder sb = new StringBuilder();
    sb.append(id).append("|").append(nombre).append("|").append(edad).append("|")
        .append(grado).append("|").append(curso).append("|");

    String notasData = notasPorCorte.entrySet().stream()
        .map(e -> e.getKey() + ":" + e.getValue().stream().map(String::valueOf).collect(Collectors.joining(",")))
        .collect(Collectors.joining(";"));

    sb.append(notasData.isEmpty() ? "NONE" : notasData).append("|")
        .append(totalFaltas).append("|").append(nombreAcudiente).append("|")
        .append(correoAcudiente).append("|").append(telefonoAcudiente).append("|");

    String materiasData = notasPorMateria.entrySet().stream()
        .filter(e -> e.getKey() != null && !e.getKey().trim().isEmpty())
        .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
        .map(e -> {
          String cortes = e.getValue().entrySet().stream()
              .map(c -> c.getKey() + ":" + c.getValue().stream().map(String::valueOf).collect(Collectors.joining(",")))
              .collect(Collectors.joining(";"));
          return e.getKey().trim() + "@" + cortes;
        })
        .collect(Collectors.joining("§"));

    sb.append(materiasData.isEmpty() ? "NONE" : materiasData).append("|");

    String faltasMesData = faltasPorMes.entrySet().stream()
        .filter(e -> e.getKey() != null && e.getValue() != null)
        .filter(e -> e.getKey() >= 1 && e.getKey() <= 12 && e.getValue() >= 0)
        .sorted((a, b) -> Integer.compare(a.getKey(), b.getKey()))
        .map(e -> e.getKey() + "=" + e.getValue())
        .collect(Collectors.joining(";"));
    sb.append(faltasMesData.isEmpty() ? "NONE" : faltasMesData).append("|");
    sb.append(mesRegistro);

    return sb.toString();
  }
}

class Institucion {
  final String nombre;
  final String usuario;
  final String password;
  final String archivo;

  public Institucion(String nombre, String usuario, String password) {
    this.nombre = nombre;
    this.usuario = usuario;
    this.password = password;
    this.archivo = "datos_" + usuario + ".txt";
  }
}

class Profesor {
  final String usuario;
  final String password;
  final String nombre;

  Profesor(String usuario, String password, String nombre) {
    this.usuario = usuario;
    this.password = password;
    this.nombre = nombre;
  }
}

class CursoAcademico {
  final String grado;
  final int curso;

  CursoAcademico(String grado, int curso) {
    this.grado = grado;
    this.curso = curso;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof CursoAcademico))
      return false;
    CursoAcademico other = (CursoAcademico) o;
    return curso == other.curso && Objects.equals(normalizarGrado(grado), normalizarGrado(other.grado));
  }

  @Override
  public int hashCode() {
    return Objects.hash(normalizarGrado(grado), curso);
  }

  @Override
  public String toString() {
    return (grado == null ? "" : grado.trim()) + " - " + curso;
  }

  static String normalizarGrado(String g) {
    return g == null ? "" : g.trim().toLowerCase();
  }
}

class Asignacion {
  final String grado;
  final int curso;
  final String materia;
  final String usuarioProfesor;

  Asignacion(String grado, int curso, String materia, String usuarioProfesor) {
    this.grado = grado;
    this.curso = curso;
    this.materia = materia;
    this.usuarioProfesor = usuarioProfesor;
  }

  CursoAcademico cursoKey() {
    return new CursoAcademico(grado, curso);
  }
}

enum TipoSesion {
  INSTITUCION,
  PROFESOR
}

public class oficial {
  static final List<String> MATERIAS_BASICAS = List.of(
      "Matemáticas",
      "Lengua Castellana",
      "Ciencias Naturales",
      "Ciencias Sociales",
      "Inglés",
      "Educación Física",
      "Tecnología e Informática",
      "Educación Artística",
      "Ética y Valores",
      "Religión");

  static final Map<Integer, String> MESES_LECTIVOS = Map.of(
      2, "Febrero",
      3, "Marzo",
      4, "Abril",
      5, "Mayo",
      6, "Junio",
      7, "Julio",
      8, "Agosto",
      9, "Septiembre",
      10, "Octubre",
      11, "Noviembre");

  private static final List<Institucion> instituciones = new ArrayList<>();
  private static final List<Estudiante> estudiantes = new ArrayList<>();
  private static final List<Profesor> profesores = new ArrayList<>();
  private static final List<Asignacion> asignaciones = new ArrayList<>();
  private static Institucion institucionActual = null;
  private static TipoSesion tipoSesion = TipoSesion.INSTITUCION;
  private static Profesor profesorActual = null;

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception e) {
      }
      inicializarSistema();
      new AppFrame().setVisible(true);
    });
  }

  private static final class AppFrame extends JFrame {
    private static final Color BG = new Color(245, 247, 250);
    private static final Color SIDEBAR = new Color(44, 62, 80);
    private static final Color BTN = new Color(52, 152, 219);
    private static final Color BTN2 = new Color(52, 73, 94);

    private final JPanel root = new JPanel(new BorderLayout());
    private final JPanel content = new JPanel(new BorderLayout());
    private final JTable tabla = new JTable();
    private final DefaultTableModel modelo = new DefaultTableModel();
    private final CardLayout rightLayout = new CardLayout();
    private final JPanel right = new JPanel(rightLayout);
    private final JPanel panelGraficas = new JPanel(new BorderLayout());
    private final java.util.Stack<String> navigationHistory = new java.util.Stack<>();

    private final JPanel loginPanel = new JPanel(new BorderLayout());
    private final JComboBox<String> cbInst = new JComboBox<>();
    private final JComboBox<String> cbTipo = new JComboBox<>(new String[] { "Institucion", "Profesor" });
    private final JTextField txtUser = new JTextField();
    private final JPasswordField txtPass = new JPasswordField();

    private final JLabel lblSesion = new JLabel("Sin sesion");
    private final JTextField txtBuscar = new JTextField();
    private final JComboBox<String> cbFiltroGrado = new JComboBox<>();
    private final JTextField txtFiltroCurso = new JTextField();
    private final JComboBox<String> cbGrafGrado = new JComboBox<>();
    private final JTextField txtGrafCurso = new JTextField();
    private final JComboBox<String> cbGrafRiesgo = new JComboBox<>(
        new String[] { "Todos", "RIESGO DE DESERCIÓN", "ALERTA DE DESERCIÓN", "NORMAL" });
    private final JLabel lblGrafInfo = new JLabel("");

    private final JTextField fRegId = new JTextField();
    private final JTextField fRegNombre = new JTextField();
    private final JTextField fRegEdad = new JTextField();
    private final JTextField fRegGrado = new JTextField();
    private final JTextField fRegCurso = new JTextField();
    private final JComboBox<String> cbRegMateria = new JComboBox<>();
    private final JTextField fRegAcud = new JTextField();
    private final JTextField fRegCorreo = new JTextField();
    private final JTextField fRegTel = new JTextField();

    private final JTextField fCursoGrado = new JTextField();
    private final JTextField fCursoCurso = new JTextField();
    private final JTextField fCursoCorte = new JTextField();
    private final JTextField fCursoCantNotas = new JTextField();

    private final JTextField fAsisGrado = new JTextField();
    private final JTextField fAsisCurso = new JTextField();
    private final JTextField fAsisDiasMax = new JTextField();

    private final JComboBox<String> cbBuscarTipo = new JComboBox<>(new String[] { "ID", "Nombre" });
    private final JTextField fBuscarQuery = new JTextField();

    private final JTextField fEditNotasId = new JTextField();
    private final JTextField fEditNotasCorte = new JTextField();
    private final JTextField fEditNotasIndice = new JTextField();
    private final JTextField fEditNotasNueva = new JTextField();

    private final JTextField fEditEstId = new JTextField();
    private final JTextField fEditEstNombre = new JTextField();
    private final JTextField fEditEstGrado = new JTextField();
    private final JTextField fEditEstCurso = new JTextField();
    private final JTextField fEditEstAcud = new JTextField();
    private final JTextField fEditEstCorreo = new JTextField();
    private final JTextField fEditEstTel = new JTextField();

    private final JTextField fEliminarId = new JTextField();

    private final JLabel lblStats = new JLabel("Seleccione Estadisticas en el panel derecho");

    private final JTable tablaCursosProf = new JTable();
    private final DefaultTableModel modeloCursosProf = new DefaultTableModel();

    private final JComboBox<String> cbProfCursoFiltro = new JComboBox<>();
    private final JComboBox<String> cbProfMateriaFiltro = new JComboBox<>();
    private final JComboBox<String> cbProfCursoNotas = new JComboBox<>();
    private final JComboBox<String> cbProfMateriaNotas = new JComboBox<>();
    private final JComboBox<String> cbProfCursoAsis = new JComboBox<>();
    private final JComboBox<String> cbProfMes = new JComboBox<>();
    private final JComboBox<String> cbProfCursoVer = new JComboBox<>();
    private final JTextField fProfCorte = new JTextField();
    private final JTextField fProfCantNotas = new JTextField();

    private final JTextField fDirProfUser = new JTextField();
    private final JTextField fDirProfPass = new JTextField();
    private final JTextField fDirProfNombre = new JTextField();

    private final JTextField fDirAsigGrado = new JTextField();
    private final JTextField fDirAsigCurso = new JTextField();
    private final JComboBox<String> cbDirAsigMateria = new JComboBox<>();
    private final JComboBox<String> cbDirAsigProfesor = new JComboBox<>();

    private void navigateTo(String panelName) {
      if (right != null && right.isShowing()) {
        navigationHistory.push(panelName);
        rightLayout.show(right, panelName);
      }
    }

    private void goBack() {
      if (right != null && right.isShowing()) {
        if (!navigationHistory.isEmpty()) {
          navigationHistory.pop();
          if (!navigationHistory.isEmpty()) {
            String previous = navigationHistory.peek();
            rightLayout.show(right, previous);
          } else {
            rightLayout.show(right, "HOME");
          }
        } else {
          rightLayout.show(right, "HOME");
        }
      } else {
        cerrarDialogoSiExiste();
      }
    }

    private void cerrarDialogoSiExiste() {
      for (Window w : Window.getWindows()) {
        if (w instanceof JDialog && w.isVisible()) {
          w.dispose();
          return;
        }
      }
    }

    private AppFrame() {
      setTitle("Sistema de Gestión Académica y Alertas Estudiantiles");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setSize(1100, 700);
      setLocationRelativeTo(null);

      root.setBackground(BG);
      setContentPane(root);

      inicializarPanelesDerechos();

      construirLogin();
      root.add(loginPanel, BorderLayout.CENTER);
    }

    private void inicializarPanelesDerechos() {
      right.removeAll();
      right.add(crearPanelHome(), "HOME");
      right.add(crearPanelMenuProfesor(), "PROF_MENU");
      right.add(crearPanelProfesorCursos(), "PROF_CURSOS");
      right.add(crearPanelProfesorNotas(), "PROF_NOTAS");
      right.add(crearPanelProfesorAsistencia(), "PROF_ASIS");
      right.add(crearPanelProfesorVerEstudiantes(), "PROF_EST");
      right.add(crearPanelMenuPsico(), "PSICO_MENU");
      right.add(crearPanelMenuDirectiva(), "DIR_MENU");
      right.add(crearPanelGraficas(), "GRAF");
      right.add(crearPanelRegistrarEstudiante(), "DOC_REG");
      right.add(crearPanelNotasMasivas(), "DOC_NOTAS");
      right.add(crearPanelAsistenciaMasiva(), "DOC_ASIS");
      right.add(crearPanelEditarNotas(), "DOC_EDIT_NOTAS");
      right.add(crearPanelBuscar(), "DOC_BUSCAR");
      right.add(crearPanelEditarEstudiante(), "DOC_EDIT_EST");
      right.add(crearPanelEliminar(), "DOC_ELIM");
      right.add(crearPanelStats(), "DIR_STATS");
      right.add(crearPanelGestionProfesores(), "DIR_PROF");
      right.add(crearPanelAsignarMaterias(), "DIR_ASIG");
      right.add(crearPanelExportarRiesgo(), "PSICO_EXPORT");

      cbProfCursoFiltro.addActionListener(e -> actualizarMateriasFiltroProfesor());
      cbProfCursoNotas.addActionListener(e -> actualizarMateriasParaCursoSeleccionado());
    }

    private void construirLogin() {
      loginPanel.removeAll();
      loginPanel.setBackground(BG);
      loginPanel.setLayout(new GridBagLayout());

      JPanel shell = new JPanel(new BorderLayout());
      shell.setBackground(Color.WHITE);
      shell.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createLineBorder(new Color(230, 235, 240), 1, true),
          BorderFactory.createEmptyBorder(0, 0, 0, 0)));

      JPanel left = new JPanel();
      left.setBackground(SIDEBAR);
      left.setPreferredSize(new Dimension(360, 0));
      left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
      left.setBorder(BorderFactory.createEmptyBorder(28, 24, 28, 24));

      JLabel brand = new JLabel("Gestion Escolar");
      brand.setForeground(Color.WHITE);
      brand.setFont(new Font("Segoe UI", Font.BOLD, 26));
      brand.setAlignmentX(JComponent.LEFT_ALIGNMENT);

      JLabel sub = new JLabel("<html>Control academico<br/>y prevencion de desercion</html>");
      sub.setForeground(new Color(236, 240, 241));
      sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
      sub.setAlignmentX(JComponent.LEFT_ALIGNMENT);

      JLabel hint = new JLabel(
          "<html><b>Acceso:</b><br/>Selecciona la institucion<br/>e ingresa tus credenciales.</html>");
      hint.setForeground(new Color(236, 240, 241));
      hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
      hint.setAlignmentX(JComponent.LEFT_ALIGNMENT);

      left.add(brand);
      left.add(Box.createVerticalStrut(10));
      left.add(sub);
      left.add(Box.createVerticalStrut(20));
      left.add(hint);
      left.add(Box.createVerticalGlue());

      JPanel rightCard = new JPanel();
      rightCard.setBackground(Color.WHITE);
      rightCard.setLayout(new BoxLayout(rightCard, BoxLayout.Y_AXIS));
      rightCard.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

      JLabel title = new JLabel("Iniciar sesion");
      title.setFont(new Font("Segoe UI", Font.BOLD, 18));
      title.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      rightCard.add(title);
      rightCard.add(Box.createVerticalStrut(18));

      cbInst.removeAllItems();
      for (Institucion i : instituciones) {
        cbInst.addItem(i.nombre);
      }

      styleInput(cbInst);
      styleInput(cbTipo);
      styleInput(txtUser);
      styleInput(txtPass);

      rightCard.add(crearCampo("Tipo de acceso", cbTipo));
      rightCard.add(Box.createVerticalStrut(12));
      rightCard.add(crearCampo("Institucion", cbInst));
      rightCard.add(Box.createVerticalStrut(12));
      rightCard.add(crearCampo("Usuario", txtUser));
      rightCard.add(Box.createVerticalStrut(12));
      rightCard.add(crearCampo("Contraseña", txtPass));
      rightCard.add(Box.createVerticalStrut(18));

      JButton btnLogin = crearBoton("Ingresar", BTN);
      btnLogin.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
      btnLogin.addActionListener(e -> login());
      rightCard.add(btnLogin);

      shell.add(left, BorderLayout.WEST);
      shell.add(rightCard, BorderLayout.CENTER);

      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.insets = new Insets(24, 24, 24, 24);
      gbc.fill = GridBagConstraints.NONE;
      loginPanel.add(shell, gbc);
    }

    private JPanel crearCampo(String label, JComponent field) {
      JPanel p = new JPanel();
      p.setOpaque(false);
      p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
      p.setAlignmentX(JComponent.LEFT_ALIGNMENT);

      JLabel l = new JLabel(label);
      l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
      l.setForeground(new Color(90, 98, 106));
      l.setAlignmentX(JComponent.LEFT_ALIGNMENT);

      field.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
      field.setPreferredSize(new Dimension(360, 36));

      p.add(l);
      p.add(Box.createVerticalStrut(6));
      p.add(field);
      return p;
    }

    private void styleInput(JComponent c) {
      c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
      c.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createLineBorder(new Color(220, 225, 230), 1, true),
          BorderFactory.createEmptyBorder(6, 10, 6, 10)));
      c.setOpaque(true);
      c.setBackground(new Color(250, 252, 255));
    }

    private JButton crearBoton(String text, Color bg) {
      JButton b = new JButton(text);
      b.setUI(new BasicButtonUI());
      b.setBackground(bg);
      b.setForeground(Color.WHITE);
      b.setOpaque(true);
      b.setContentAreaFilled(true);
      b.setBorderPainted(false);
      b.setFocusPainted(false);
      b.setFont(new Font("Segoe UI", Font.BOLD, 13));
      b.setCursor(new Cursor(Cursor.HAND_CURSOR));
      b.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
      return b;
    }

    private void login() {
      int idx = cbInst.getSelectedIndex();
      if (idx < 0)
        return;
      Institucion inst = instituciones.get(idx);

      String user = txtUser.getText().trim();
      String pass = new String(txtPass.getPassword());
      String tipo = (String) cbTipo.getSelectedItem();
      boolean esProfesor = "Profesor".equalsIgnoreCase(tipo);

      if (esProfesor) {
        List<Profesor> profTmp = new ArrayList<>();
        cargarProfesores(inst, profTmp);
        Optional<Profesor> opt = profTmp.stream()
            .filter(p -> p.usuario.equalsIgnoreCase(user) && p.password.equals(pass))
            .findFirst();
        if (opt.isEmpty()) {
          JOptionPane.showMessageDialog(this, "Credenciales de profesor incorrectas.", "Error",
              JOptionPane.ERROR_MESSAGE);
          return;
        }
        institucionActual = inst;
        tipoSesion = TipoSesion.PROFESOR;
        profesorActual = opt.get();
        estudiantes.clear();
        cargarDatos();
        profesores.clear();
        profesores.addAll(profTmp);
        asignaciones.clear();
        cargarAsignaciones(inst, asignaciones);
      } else {
        if (!inst.usuario.equals(user) || !inst.password.equals(pass)) {
          JOptionPane.showMessageDialog(this, "Credenciales incorrectas.", "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        institucionActual = inst;
        tipoSesion = TipoSesion.INSTITUCION;
        profesorActual = null;
        estudiantes.clear();
        cargarDatos();
        cargarProfesoresYAsignaciones();
      }

      construirAplicacion();
      root.remove(loginPanel);
      root.add(content, BorderLayout.CENTER);
      root.revalidate();
      root.repaint();
      refrescarTabla(estudiantesVisibles());
    }

    private void construirAplicacion() {
      content.removeAll();
      if (tipoSesion == TipoSesion.PROFESOR) {
        construirAplicacionProfesor();
        return;
      }

      // JTabbedPane structure exactly as requested!
      content.setLayout(new BorderLayout());

      JPanel top = new JPanel(new BorderLayout());
      top.setBackground(BG);
      top.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

      JPanel title = new JPanel(new BorderLayout());
      title.setBackground(BG);
      JLabel l = new JLabel("🏫");
      l.setFont(new Font("Segoe UI", Font.PLAIN, 32));
      title.add(l, BorderLayout.WEST);

      JPanel info = new JPanel(new BorderLayout());
      info.setBackground(BG);
      JLabel h1 = new JLabel("Sistema de Gestión de Deserción Estudiantil");
      h1.setFont(new Font("Segoe UI", Font.BOLD, 18));
      h1.setForeground(new Color(41, 128, 185));
      lblSesion.setText(institucionActual != null ? ("Institución: " + institucionActual.nombre) : "Sin sesión");
      lblSesion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
      lblSesion.setForeground(new Color(52, 73, 94));
      info.add(h1, BorderLayout.NORTH);
      info.add(lblSesion, BorderLayout.SOUTH);
      title.add(info, BorderLayout.CENTER);

      JButton bLogout = crearBoton("CERRAR SESION", new Color(231, 76, 60));
      bLogout.addActionListener(e -> logout());
      title.add(bLogout, BorderLayout.EAST);
      top.add(title, BorderLayout.CENTER);

      content.add(top, BorderLayout.NORTH);

      final JTabbedPane tabbedPane = new JTabbedPane();
      tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

      // Tab 1: Directiva (complete menu + students)
      JPanel tabDirectiva = crearTabDirectiva();
      tabbedPane.addTab("Directiva", tabDirectiva);

      // Tab 2: Reportar Alerta
      JPanel tabReportar = crearTabReportarAlerta();
      tabbedPane.addTab("Reportar Alerta", tabReportar);

      // Tab 3: Análisis y Estadísticas
      JPanel tabAnalisis = crearTabAnalisisEstadisticas();
      tabbedPane.addTab("Análisis y Estadísticas", tabAnalisis);

      // History for "Volver" functionality
      final Stack<Integer> tabHistory = new Stack<>();
      tabHistory.push(0);

      tabbedPane.addChangeListener(e -> {
        int newIndex = tabbedPane.getSelectedIndex();
        if (newIndex != -1 && newIndex != tabHistory.peek()) {
          tabHistory.push(newIndex);
        }
      });

      content.add(tabbedPane, BorderLayout.CENTER);
      refrescarTabla(estudiantesVisibles());
    }

    private JPanel crearTabDirectiva() {
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBackground(BG);

      JPanel center = new JPanel(new BorderLayout());
      center.setBackground(BG);
      center.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

      JPanel sidebar = new JPanel();
      sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
      sidebar.setBackground(SIDEBAR);
      sidebar.setPreferredSize(new Dimension(240, 0));
      sidebar.setBorder(BorderFactory.createEmptyBorder(18, 12, 18, 12));

      JLabel brand = new JLabel("DIRECTIVA");
      brand.setForeground(Color.WHITE);
      brand.setFont(new Font("Segoe UI", Font.BOLD, 18));
      brand.setAlignmentX(JComponent.CENTER_ALIGNMENT);
      sidebar.add(brand);
      sidebar.add(Box.createVerticalStrut(10));

      JButton b1 = crearBoton("Estadísticas", BTN);
      JButton b2 = crearBoton("Gestionar Profesores", BTN2);
      JButton b3 = crearBoton("Asignar Materias", BTN2);
      JButton b4 = crearBoton("Registrar Estudiante", BTN2);
      JButton b5 = crearBoton("Editar", BTN2);
      JButton b6 = crearBoton("Eliminar", BTN2);
      JButton b7 = crearBoton("Buscar", BTN2);

      for (JButton b : new JButton[] { b1, b2, b3, b4, b5, b6, b7 }) {
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        sidebar.add(b);
        sidebar.add(Box.createVerticalStrut(10));
      }

      JPanel centerStudents = new JPanel(new BorderLayout());
      centerStudents.setBackground(BG);

      JPanel headerStudents = new JPanel(new BorderLayout());
      headerStudents.setBackground(BG);
      JLabel hStudents = new JLabel("Estudiantes");
      hStudents.setFont(new Font("Segoe UI", Font.BOLD, 18));
      headerStudents.add(hStudents, BorderLayout.WEST);

      JPanel filtrosStudents = new JPanel();
      filtrosStudents.setBackground(BG);
      filtrosStudents.setLayout(new BoxLayout(filtrosStudents, BoxLayout.X_AXIS));

      txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
      txtBuscar.setPreferredSize(new Dimension(220, 32));
      txtBuscar.setMaximumSize(new Dimension(260, 32));
      txtBuscar.setToolTipText("Buscar por ID o Nombre");

      actualizarCombosGrado();
      cbFiltroGrado.setMaximumSize(new Dimension(140, 32));

      txtFiltroCurso.setFont(new Font("Segoe UI", Font.PLAIN, 13));
      txtFiltroCurso.setPreferredSize(new Dimension(90, 32));
      txtFiltroCurso.setMaximumSize(new Dimension(90, 32));
      txtFiltroCurso.setToolTipText("Curso (opcional)");

      JButton bAplicar = crearBoton("Filtrar", BTN);
      JButton bReset = crearBoton("Reset", BTN2);

      bAplicar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
      bReset.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

      filtrosStudents.add(new JLabel("Buscar: "));
      filtrosStudents.add(Box.createHorizontalStrut(6));
      filtrosStudents.add(txtBuscar);
      filtrosStudents.add(Box.createHorizontalStrut(10));
      filtrosStudents.add(new JLabel("Grado: "));
      filtrosStudents.add(Box.createHorizontalStrut(6));
      filtrosStudents.add(cbFiltroGrado);
      filtrosStudents.add(Box.createHorizontalStrut(10));
      filtrosStudents.add(new JLabel("Curso: "));
      filtrosStudents.add(Box.createHorizontalStrut(6));
      filtrosStudents.add(txtFiltroCurso);
      filtrosStudents.add(Box.createHorizontalStrut(10));
      filtrosStudents.add(bAplicar);
      filtrosStudents.add(Box.createHorizontalStrut(8));
      filtrosStudents.add(bReset);

      headerStudents.add(filtrosStudents, BorderLayout.EAST);

      centerStudents.add(headerStudents, BorderLayout.NORTH);

      final JTable tablaDirectiva = new JTable();
      final DefaultTableModel modeloDirectiva = new DefaultTableModel();
      tablaDirectiva.setModel(modeloDirectiva);
      tablaDirectiva.setRowHeight(24);
      tablaDirectiva.setFont(new Font("Segoe UI", Font.PLAIN, 12));
      tablaDirectiva.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
      centerStudents.add(new JScrollPane(tablaDirectiva), BorderLayout.CENTER);

      JSplitPane fullSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, centerStudents);
      fullSplit.setResizeWeight(0.2);
      fullSplit.setBorder(null);

      center.add(fullSplit, BorderLayout.CENTER);

      b1.addActionListener(e -> {
        actualizarStats();
        JOptionPane.showMessageDialog(this, lblStats.getText(), "Estadísticas", JOptionPane.INFORMATION_MESSAGE);
      });
      b2.addActionListener(e -> {
        prepararPanelDirectivaProfesores();
        mostrarPanelEnDialogo(crearPanelGestionProfesores(), "Gestionar Profesores");
      });
      b3.addActionListener(e -> {
        prepararPanelDirectivaAsignaciones();
        mostrarPanelEnDialogo(crearPanelAsignarMaterias(), "Asignar Materias");
      });
      b4.addActionListener(e -> {
        mostrarPanelEnDialogo(crearPanelRegistrarEstudiante(), "Registrar Estudiante");
      });
      b5.addActionListener(e -> {
        mostrarPanelEnDialogo(crearPanelEditarEstudiante(), "Editar Estudiante");
      });
      b6.addActionListener(e -> {
        mostrarPanelEnDialogo(crearPanelEliminar(), "Eliminar Estudiante");
      });
      b7.addActionListener(e -> {
        mostrarPanelEnDialogo(crearPanelBuscar(), "Buscar Estudiante");
      });

      bAplicar.addActionListener(ev -> {
        List<Estudiante> filtrados = estudiantes.stream()
            .filter(est -> {
              String q = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
              return q.isEmpty()
                  || String.valueOf(est.getId()).contains(q)
                  || est.getNombre().toLowerCase().contains(q);
            })
            .filter(est -> {
              String grado = (String) cbFiltroGrado.getSelectedItem();
              return grado == null || grado.equals("Todos") || est.getGrado().equalsIgnoreCase(grado);
            })
            .filter(est -> {
              String cursoStr = txtFiltroCurso.getText() == null ? "" : txtFiltroCurso.getText().trim();
              if (cursoStr.isEmpty())
                return true;
              try {
                int curso = Integer.parseInt(cursoStr);
                return est.getCurso() == curso;
              } catch (Exception ex) {
                return true;
              }
            })
            .collect(Collectors.toList());
        refrescarTablaEnTabla(filtrados, modeloDirectiva, tablaDirectiva);
      });
      bReset.addActionListener(e -> {
        txtBuscar.setText("");
        cbFiltroGrado.setSelectedIndex(0);
        txtFiltroCurso.setText("");
        refrescarTablaEnTabla(estudiantesVisibles(), modeloDirectiva, tablaDirectiva);
      });

      panel.add(center, BorderLayout.CENTER);
      refrescarTablaEnTabla(estudiantesVisibles(), modeloDirectiva, tablaDirectiva);

      return panel;
    }

    private JPanel crearTabReportarAlerta() {
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBackground(BG);

      JPanel center = new JPanel(new BorderLayout());
      center.setBackground(BG);
      center.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

      JPanel sidebar = new JPanel();
      sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
      sidebar.setBackground(SIDEBAR);
      sidebar.setPreferredSize(new Dimension(240, 0));
      sidebar.setBorder(BorderFactory.createEmptyBorder(18, 12, 18, 12));

      JLabel brand = new JLabel("PSICOSOCIAL");
      brand.setForeground(Color.WHITE);
      brand.setFont(new Font("Segoe UI", Font.BOLD, 18));
      brand.setAlignmentX(JComponent.CENTER_ALIGNMENT);
      sidebar.add(brand);
      sidebar.add(Box.createVerticalStrut(10));

      JButton b1 = crearBoton("Ver Estudiantes en Riesgo", BTN);
      JButton b2 = crearBoton("Exportar Lista de Riesgo", BTN2);
      JButton b3 = crearBoton("Enviar Correo de Alerta", BTN2);

      for (JButton b : new JButton[] { b1, b2, b3 }) {
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        sidebar.add(b);
        sidebar.add(Box.createVerticalStrut(10));
      }

      JPanel centerStudents = new JPanel(new BorderLayout());
      centerStudents.setBackground(BG);

      JPanel headerStudents = new JPanel(new BorderLayout());
      headerStudents.setBackground(BG);
      JLabel hStudents = new JLabel("Estudiantes en Riesgo");
      hStudents.setFont(new Font("Segoe UI", Font.BOLD, 18));
      headerStudents.add(hStudents, BorderLayout.WEST);

      centerStudents.add(headerStudents, BorderLayout.NORTH);

      final JTable tablaPsico = new JTable();
      final DefaultTableModel modeloPsico = new DefaultTableModel();
      tablaPsico.setModel(modeloPsico);
      tablaPsico.setRowHeight(24);
      tablaPsico.setFont(new Font("Segoe UI", Font.PLAIN, 12));
      tablaPsico.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
      centerStudents.add(new JScrollPane(tablaPsico), BorderLayout.CENTER);

      JSplitPane fullSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, centerStudents);
      fullSplit.setResizeWeight(0.2);
      fullSplit.setBorder(null);

      center.add(fullSplit, BorderLayout.CENTER);
      panel.add(center, BorderLayout.CENTER);

      b1.addActionListener(e -> {
        List<Estudiante> riesgo = estudiantes.stream()
            .filter(est -> est.getNivelRiesgo().equals("RIESGO DE DESERCIÓN") ||
                est.getNivelRiesgo().equals("ALERTA DE DESERCIÓN"))
            .collect(Collectors.toList());
        refrescarTablaEnTabla(riesgo, modeloPsico, tablaPsico);
      });
      b2.addActionListener(e -> accionExportarRiesgo());
      b3.addActionListener(e -> mostrarDialogoCorreo());

      refrescarTablaEnTabla(estudiantes.stream()
          .filter(est -> est.getNivelRiesgo().equals("RIESGO DE DESERCIÓN") ||
              est.getNivelRiesgo().equals("ALERTA DE DESERCIÓN"))
          .collect(Collectors.toList()), modeloPsico, tablaPsico);

      return panel;
    }

    private JPanel crearTabAnalisisEstadisticas() {
      return crearTabGraficas();
    }

    private JPanel crearTabGraficas() {
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBackground(BG);
      panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

      // Create the graphs panel components
      JPanel top = new JPanel(new GridLayout(2, 1, 8, 8));
      top.setOpaque(false);

      JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
      row1.setOpaque(false);
      row1.add(new JLabel("Grado:"));
      actualizarCombosGrado();
      row1.add(cbGrafGrado);
      row1.add(new JLabel("Curso:"));
      txtGrafCurso.setPreferredSize(new Dimension(80, 28));
      row1.add(txtGrafCurso);
      row1.add(new JLabel("Estado:"));
      row1.add(cbGrafRiesgo);

      JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
      row2.setOpaque(false);
      row2.add(new JLabel("Buscar:"));
      JTextField txtGrafBuscar = new JTextField();
      txtGrafBuscar.setPreferredSize(new Dimension(200, 28));
      row2.add(txtGrafBuscar);
      row2.add(new JLabel("Ancho de gráficas:"));
      JTextField fWidth = new JTextField("800");
      fWidth.setPreferredSize(new Dimension(100, 28));
      row2.add(fWidth);
      JButton bDescargar = crearBoton("Descargar Imagen", BTN);
      bDescargar.addActionListener(e -> descargarGrafica(fWidth.getText()));
      row2.add(bDescargar);
      lblGrafInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
      lblGrafInfo.setForeground(new Color(90, 98, 106));
      row2.add(lblGrafInfo);

      top.add(row1);
      top.add(Box.createVerticalStrut(8));
      top.add(row2);

      panelGraficas.setOpaque(false);

      // Stats panel with acceleration calculation
      JPanel statsPanel = new JPanel(new BorderLayout());
      statsPanel.setBackground(Color.WHITE);
      statsPanel.setBorder(BorderFactory.createTitledBorder(
          BorderFactory.createLineBorder(new Color(41, 128, 185), 1),
          "Análisis de Aceleración",
          javax.swing.border.TitledBorder.LEFT,
          javax.swing.border.TitledBorder.TOP,
          new Font("Segoe UI", Font.BOLD, 14),
          new Color(41, 128, 185)));
      statsPanel.setPreferredSize(new Dimension(350, 0));

      actualizarStats();
      lblStats.setFont(new Font("Segoe UI", Font.PLAIN, 12));
      statsPanel.add(lblStats, BorderLayout.CENTER);

      JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, statsPanel, panelGraficas);
      split.setResizeWeight(0.3);
      split.setBorder(null);

      JPanel body = new JPanel(new BorderLayout());
      body.setOpaque(false);
      body.add(top, BorderLayout.NORTH);
      body.add(split, BorderLayout.CENTER);

      panel.add(body, BorderLayout.CENTER);

      // Load initial graphs
      mostrarGraficasEnPantalla();

      return panel;
    }

    private List<Estudiante> estudiantesVisibles() {
      if (tipoSesion == TipoSesion.PROFESOR && profesorActual != null) {
        return estudiantesAsignadosProfesor(profesorActual);
      }
      return estudiantes;
    }

    private void construirAplicacionProfesor() {
      content.removeAll();
      JPanel sidebar = new JPanel();
      sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
      sidebar.setBackground(SIDEBAR);
      sidebar.setPreferredSize(new Dimension(240, 0));
      sidebar.setBorder(BorderFactory.createEmptyBorder(18, 12, 18, 12));

      JLabel brand = new JLabel("PROFESOR");
      brand.setForeground(Color.WHITE);
      brand.setFont(new Font("Segoe UI", Font.BOLD, 18));
      brand.setAlignmentX(JComponent.CENTER_ALIGNMENT);
      sidebar.add(brand);
      sidebar.add(Box.createVerticalStrut(10));

      String pNombre = profesorActual == null ? "" : profesorActual.nombre;
      lblSesion.setText("<html>Institucion: " + institucionActual.nombre + "<br/>Profesor: " + pNombre + "</html>");
      lblSesion.setForeground(new Color(236, 240, 241));
      lblSesion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
      lblSesion.setAlignmentX(JComponent.CENTER_ALIGNMENT);
      sidebar.add(lblSesion);
      sidebar.add(Box.createVerticalStrut(18));

      JButton bMenu = crearBoton("MI MENU", BTN2);
      JButton bCursos = crearBoton("MIS CURSOS", BTN);
      JButton bNotas = crearBoton("REGISTRAR NOTAS", BTN);
      JButton bAsis = crearBoton("REGISTRAR ASIST.", BTN);
      JButton bVerEst = crearBoton("VER ESTUDIANTES", BTN2);
      JButton bLogout = crearBoton("CERRAR SESION", new Color(231, 76, 60));

      for (JButton b : new JButton[] { bMenu, bCursos, bNotas, bAsis, bVerEst, bLogout }) {
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        sidebar.add(b);
        sidebar.add(Box.createVerticalStrut(10));
      }

      JPanel main = new JPanel(new BorderLayout());
      main.setBackground(BG);
      main.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

      JPanel header = new JPanel(new BorderLayout());
      header.setBackground(BG);
      JLabel h = new JLabel("Estudiantes asignados");
      h.setFont(new Font("Segoe UI", Font.BOLD, 18));
      header.add(h, BorderLayout.WEST);

      JPanel filtros = new JPanel();
      filtros.setBackground(BG);
      filtros.setLayout(new BoxLayout(filtros, BoxLayout.X_AXIS));

      txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
      txtBuscar.setPreferredSize(new Dimension(220, 32));
      txtBuscar.setMaximumSize(new Dimension(260, 32));
      txtBuscar.setToolTipText("Buscar por ID o Nombre");

      actualizarCombosProfesorCursosFiltro();
      cbProfCursoFiltro.setMaximumSize(new Dimension(180, 32));
      cbProfMateriaFiltro.setMaximumSize(new Dimension(180, 32));

      JButton bAplicar = crearBoton("Filtrar", BTN);
      JButton bReset = crearBoton("Reset", BTN2);
      bAplicar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
      bReset.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

      filtros.add(new JLabel("Buscar: "));
      filtros.add(Box.createHorizontalStrut(6));
      filtros.add(txtBuscar);
      filtros.add(Box.createHorizontalStrut(10));
      filtros.add(new JLabel("Curso: "));
      filtros.add(Box.createHorizontalStrut(6));
      filtros.add(cbProfCursoFiltro);
      filtros.add(Box.createHorizontalStrut(10));
      filtros.add(new JLabel("Materia: "));
      filtros.add(Box.createHorizontalStrut(6));
      filtros.add(cbProfMateriaFiltro);
      filtros.add(Box.createHorizontalStrut(10));
      filtros.add(bAplicar);
      filtros.add(Box.createHorizontalStrut(8));
      filtros.add(bReset);

      header.add(filtros, BorderLayout.EAST);
      main.add(header, BorderLayout.NORTH);

      tabla.setModel(modelo);
      tabla.setRowHeight(24);
      tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
      tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
      main.add(new JScrollPane(tabla), BorderLayout.CENTER);

      bMenu.addActionListener(e -> navigateTo("PROF_MENU"));
      bCursos.addActionListener(e -> navigateTo("PROF_CURSOS"));
      bNotas.addActionListener(e -> {
        prepararPanelProfesorNotas();
        navigateTo("PROF_NOTAS");
      });
      bAsis.addActionListener(e -> {
        prepararPanelProfesorAsistencia();
        navigateTo("PROF_ASIS");
      });
      bVerEst.addActionListener(e -> {
        prepararPanelProfesorVerEstudiantes();
        navigateTo("PROF_EST");
      });
      bLogout.addActionListener(e -> logout());

      bAplicar.addActionListener(e -> aplicarFiltrosProfesor());
      bReset.addActionListener(e -> {
        txtBuscar.setText("");
        cbProfCursoFiltro.setSelectedIndex(0);
        cbProfMateriaFiltro.setSelectedIndex(0);
        refrescarTabla(estudiantesVisibles());
      });

      right.setPreferredSize(new Dimension(420, 0));
      navigationHistory.push("PROF_MENU");
      rightLayout.show(right, "PROF_MENU");

      content.add(sidebar, BorderLayout.WEST);
      content.add(main, BorderLayout.CENTER);
      content.add(right, BorderLayout.EAST);

      refrescarTabla(estudiantesVisibles());
    }

    private void logout() {
      institucionActual = null;
      estudiantes.clear();
      profesores.clear();
      asignaciones.clear();
      tipoSesion = TipoSesion.INSTITUCION;
      profesorActual = null;
      txtUser.setText("");
      txtPass.setText("");
      content.removeAll();
      root.remove(content);
      root.add(loginPanel, BorderLayout.CENTER);
      root.revalidate();
      root.repaint();
    }

    private void refrescarTabla(List<Estudiante> lista) {
      if (lista == null)
        return;

      List<Estudiante> listaOrdenada = new ArrayList<>(lista);
      listaOrdenada.sort((e1, e2) -> {
        int g1 = obtenerPesoGrado(e1.getGrado());
        int g2 = obtenerPesoGrado(e2.getGrado());
        if (g1 != g2)
          return Integer.compare(g1, g2);

        int c = Integer.compare(e1.getCurso(), e2.getCurso());
        if (c != 0)
          return c;

        String n1 = e1.getNombre() == null ? "" : e1.getNombre();
        String n2 = e2.getNombre() == null ? "" : e2.getNombre();
        return n1.compareToIgnoreCase(n2);
      });

      tabla.setSelectionBackground(new Color(232, 241, 250));
      tabla.setSelectionForeground(Color.BLACK);
      tabla.setShowGrid(true);
      tabla.setGridColor(new Color(230, 230, 230));
      tabla.getTableHeader().setBackground(new Color(240, 240, 240));
      tabla.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));

      if (tipoSesion == TipoSesion.PROFESOR) {
        refrescarTablaVistaProfesor(listaOrdenada);
        return;
      }

      List<String> columnas = new ArrayList<>();
      columnas.add("ID");
      columnas.add("Nombre");
      columnas.add("Grado");
      columnas.add("Curso");
      columnas.add("Perdidas C1");
      columnas.add("Perdidas C2");
      columnas.add("Perdidas C3");
      columnas.add("Promedio");
      columnas.add("Faltas");
      columnas.add("Estado");
      columnas.add("Acudiente");
      columnas.add("Telefono");
      columnas.add("Correo");

      modelo.setColumnIdentifiers(columnas.toArray(new String[0]));
      modelo.setRowCount(0);
      for (Estudiante e : listaOrdenada) {
        Object[] row = new Object[columnas.size()];
        int k = 0;
        row[k++] = e.getId();
        row[k++] = e.getNombre();
        row[k++] = e.getGrado();
        row[k++] = e.getCurso();
        row[k++] = e.contarMateriasPerdidasCorte(1);
        row[k++] = e.contarMateriasPerdidasCorte(2);
        row[k++] = e.contarMateriasPerdidasCorte(3);
        row[k++] = String.format("%.2f", e.getPromedioGeneral());
        row[k++] = e.getTotalFaltas();
        row[k++] = e.getNivelRiesgo();
        row[k++] = e.getNombreAcudiente();
        row[k++] = e.getTelefonoAcudiente();
        row[k++] = e.getCorreoAcudiente();
        modelo.addRow(row);
      }
    }

    private void refrescarTablaVistaProfesor(List<Estudiante> lista) {
      String materia = (String) cbProfMateriaFiltro.getSelectedItem();
      if (materia == null || materia.equalsIgnoreCase("Seleccione materia")) {
        modelo.setColumnIdentifiers(
            new String[] { "ID", "Nombre", "Grado", "Curso", "Seleccione una materia para ver notas" });
        modelo.setRowCount(0);
        for (Estudiante e : lista) {
          modelo.addRow(new Object[] { e.getId(), e.getNombre(), e.getGrado(), e.getCurso(), "" });
        }
        return;
      }

      int maxC1 = 0, maxC2 = 0, maxC3 = 0;
      for (Estudiante e : lista) {
        Map<Integer, List<Double>> cortes = e.getNotasPorMateria().get(materia);
        if (cortes != null) {
          if (cortes.get(1) != null)
            maxC1 = Math.max(maxC1, cortes.get(1).size());
          if (cortes.get(2) != null)
            maxC2 = Math.max(maxC2, cortes.get(2).size());
          if (cortes.get(3) != null)
            maxC3 = Math.max(maxC3, cortes.get(3).size());
        }
      }

      List<String> columnas = new ArrayList<>();
      columnas.add("ID");
      columnas.add("Nombre");
      for (int i = 1; i <= maxC1; i++)
        columnas.add("C1-N" + i);
      columnas.add("Prom C1");
      for (int i = 1; i <= maxC2; i++)
        columnas.add("C2-N" + i);
      columnas.add("Prom C2");
      for (int i = 1; i <= maxC3; i++)
        columnas.add("C3-N" + i);
      columnas.add("Prom C3");

      columnas.add("Inasistencias");
      columnas.add("Acudiente");
      columnas.add("Telefono");
      columnas.add("Correo");

      modelo.setColumnIdentifiers(columnas.toArray(new String[0]));
      modelo.setRowCount(0);

      for (Estudiante e : lista) {
        Object[] row = new Object[columnas.size()];
        int k = 0;
        row[k++] = e.getId();
        row[k++] = e.getNombre();

        Map<Integer, List<Double>> cortes = e.getNotasPorMateria().get(materia);

        List<Double> n1 = (cortes != null) ? cortes.get(1) : null;
        for (int i = 0; i < maxC1; i++) {
          row[k++] = (n1 != null && i < n1.size()) ? String.format("%.2f", n1.get(i)) : "-";
        }
        row[k++] = (n1 != null && !n1.isEmpty())
            ? String.format("%.2f", n1.stream().mapToDouble(d -> d).average().orElse(0.0))
            : "-";

        List<Double> n2 = (cortes != null) ? cortes.get(2) : null;
        for (int i = 0; i < maxC2; i++) {
          row[k++] = (n2 != null && i < n2.size()) ? String.format("%.2f", n2.get(i)) : "-";
        }
        row[k++] = (n2 != null && !n2.isEmpty())
            ? String.format("%.2f", n2.stream().mapToDouble(d -> d).average().orElse(0.0))
            : "-";

        List<Double> n3 = (cortes != null) ? cortes.get(3) : null;
        for (int i = 0; i < maxC3; i++) {
          row[k++] = (n3 != null && i < n3.size()) ? String.format("%.2f", n3.get(i)) : "-";
        }
        row[k++] = (n3 != null && !n3.isEmpty())
            ? String.format("%.2f", n3.stream().mapToDouble(d -> d).average().orElse(0.0))
            : "-";

        row[k++] = e.getTotalFaltas();
        row[k++] = e.getNombreAcudiente();
        row[k++] = e.getTelefonoAcudiente();
        row[k++] = e.getCorreoAcudiente();
        modelo.addRow(row);
      }
    }

    private int obtenerPesoGrado(String grado) {
      if (grado == null)
        return 99;
      String g = Estudiante.normalizarTexto(grado);
      if (g.contains("jardin"))
        return 0;
      if (g.contains("transicion"))
        return 1;
      try {
        String num = g.replaceAll("[^0-9]", "");
        if (!num.isEmpty()) {
          return Integer.parseInt(num) + 2;
        }
      } catch (Exception e) {
      }
      return 99;
    }

    private void aplicarFiltrosProfesor() {
      String q = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
      String cursoSel = (String) cbProfCursoFiltro.getSelectedItem();
      CursoAcademico curso = parseCursoDisplay(cursoSel);

      List<Estudiante> base = estudiantesVisibles();
      List<Estudiante> filtrados = base.stream()
          .filter(e -> q.isEmpty()
              || String.valueOf(e.getId()).contains(q)
              || e.getNombre().toLowerCase().contains(q))
          .filter(e -> curso == null
              || (CursoAcademico.normalizarGrado(e.getGrado()).equals(CursoAcademico.normalizarGrado(curso.grado))
                  && e.getCurso() == curso.curso))
          .toList();
      refrescarTabla(filtrados);
    }

    private void actualizarCombosProfesorCursosFiltro() {
      actualizarComboCursos(cbProfCursoFiltro, true);
      actualizarMateriasFiltroProfesor();
    }

    private void actualizarMateriasFiltroProfesor() {
      CursoAcademico curso = parseCursoDisplay((String) cbProfCursoFiltro.getSelectedItem());
      cbProfMateriaFiltro.removeAllItems();
      cbProfMateriaFiltro.addItem("Seleccione materia");
      if (curso == null || profesorActual == null) {
        return;
      }
      Set<String> mats = asignaciones.stream()
          .filter(a -> a != null && profesorActual.usuario.equals(a.usuarioProfesor))
          .filter(a -> a.cursoKey().equals(curso))
          .map(a -> a.materia)
          .filter(m -> m != null && !m.trim().isEmpty())
          .collect(Collectors.toSet());
      mats.stream().sorted(String.CASE_INSENSITIVE_ORDER).forEach(cbProfMateriaFiltro::addItem);
    }

    private void prepararPanelProfesorNotas() {
      actualizarComboCursos(cbProfCursoNotas, false);
      actualizarMateriasParaCursoSeleccionado();
    }

    private void prepararPanelProfesorAsistencia() {
      actualizarComboCursos(cbProfCursoAsis, false);
      cbProfMes.removeAllItems();
      for (Map.Entry<Integer, String> e : oficial.MESES_LECTIVOS.entrySet().stream()
          .sorted((a, b) -> Integer.compare(a.getKey(), b.getKey()))
          .toList()) {
        cbProfMes.addItem(e.getKey() + " - " + e.getValue());
      }
    }

    private void prepararPanelProfesorVerEstudiantes() {
      actualizarComboCursos(cbProfCursoVer, false);
    }

    private void actualizarComboCursos(JComboBox<String> combo, boolean incluirTodos) {
      combo.removeAllItems();
      if (incluirTodos) {
        combo.addItem("Todos");
      }
      for (CursoAcademico c : cursosAsignadosProfesorOrdenados()) {
        combo.addItem(c.toString());
      }
      if (combo.getItemCount() == 0 && incluirTodos) {
        combo.addItem("Todos");
      }
    }

    private List<CursoAcademico> cursosAsignadosProfesorOrdenados() {
      if (profesorActual == null) {
        return List.of();
      }
      Map<CursoAcademico, Boolean> seen = new HashMap<>();
      for (Asignacion a : asignaciones) {
        if (a == null)
          continue;
        if (!profesorActual.usuario.equals(a.usuarioProfesor))
          continue;
        CursoAcademico c = a.cursoKey();
        seen.put(c, true);
      }
      List<CursoAcademico> list = new ArrayList<>(seen.keySet());
      list.sort((a, b) -> {
        int g = CursoAcademico.normalizarGrado(a.grado).compareTo(CursoAcademico.normalizarGrado(b.grado));
        if (g != 0)
          return g;
        return Integer.compare(a.curso, b.curso);
      });
      return list;
    }

    private void actualizarMateriasParaCursoSeleccionado() {
      CursoAcademico curso = parseCursoDisplay((String) cbProfCursoNotas.getSelectedItem());
      cbProfMateriaNotas.removeAllItems();
      if (curso == null || profesorActual == null) {
        return;
      }
      Set<String> mats = asignaciones.stream()
          .filter(a -> a != null && profesorActual.usuario.equals(a.usuarioProfesor))
          .filter(a -> a.cursoKey().equals(curso))
          .map(a -> a.materia)
          .filter(m -> m != null && !m.trim().isEmpty())
          .collect(Collectors.toSet());
      mats.stream().sorted(String.CASE_INSENSITIVE_ORDER).forEach(cbProfMateriaNotas::addItem);
    }

    private CursoAcademico parseCursoDisplay(String s) {
      if (s == null)
        return null;
      String t = s.trim();
      if (t.isEmpty() || "Todos".equalsIgnoreCase(t))
        return null;
      int idx = t.lastIndexOf(" - ");
      if (idx < 0)
        return null;
      String grado = t.substring(0, idx).trim();
      String cursoStr = t.substring(idx + 3).trim();
      try {
        int curso = Integer.parseInt(cursoStr);
        return new CursoAcademico(grado, curso);
      } catch (Exception e) {
        return null;
      }
    }

    private Integer parseMesDisplay(String s) {
      if (s == null)
        return null;
      String t = s.trim();
      if (t.isEmpty())
        return null;
      int idx = t.indexOf(" - ");
      if (idx < 0)
        return null;
      try {
        int mes = Integer.parseInt(t.substring(0, idx).trim());
        if (!oficial.MESES_LECTIVOS.containsKey(mes))
          return null;
        return mes;
      } catch (Exception e) {
        return null;
      }
    }

    private JPanel crearPanelGraficas() {
      JPanel p = crearPanelBase("Graficas");
      JPanel body = new JPanel(new BorderLayout());
      body.setOpaque(false);

      JButton back = crearBoton("Volver", BTN2);
      back.addActionListener(e -> goBack());

      JPanel top = new JPanel();
      top.setOpaque(false);
      top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

      JPanel row1 = new JPanel();
      row1.setOpaque(false);
      row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));

      JLabel lGrado = new JLabel("Grado:");
      lGrado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
      row1.add(lGrado);
      row1.add(Box.createHorizontalStrut(6));
      cbGrafGrado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
      cbGrafGrado.setMaximumSize(new Dimension(140, 30));
      row1.add(cbGrafGrado);

      row1.add(Box.createHorizontalStrut(10));
      JLabel lCurso = new JLabel("Curso:");
      lCurso.setFont(new Font("Segoe UI", Font.PLAIN, 12));
      row1.add(lCurso);
      row1.add(Box.createHorizontalStrut(6));
      txtGrafCurso.setFont(new Font("Segoe UI", Font.PLAIN, 12));
      txtGrafCurso.setMaximumSize(new Dimension(90, 30));
      row1.add(txtGrafCurso);

      row1.add(Box.createHorizontalStrut(10));
      JLabel lR = new JLabel("Estado:");
      lR.setFont(new Font("Segoe UI", Font.PLAIN, 12));
      row1.add(lR);
      row1.add(Box.createHorizontalStrut(6));
      cbGrafRiesgo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
      cbGrafRiesgo.setMaximumSize(new Dimension(110, 30));
      row1.add(cbGrafRiesgo);

      row1.add(Box.createHorizontalStrut(10));
      JButton bUpd = crearBoton("Actualizar", BTN);
      bUpd.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
      bUpd.addActionListener(e -> mostrarGraficasEnPantalla());
      row1.add(bUpd);

      row1.add(Box.createHorizontalGlue());
      row1.add(back);

      JPanel row2 = new JPanel();
      row2.setOpaque(false);
      row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));

      JLabel lWidth = new JLabel("Ancho de gráficas:");
      lWidth.setFont(new Font("Segoe UI", Font.PLAIN, 12));
      row2.add(lWidth);
      row2.add(Box.createHorizontalStrut(6));

      JTextField txtGrafWidth = new JTextField("800");
      txtGrafWidth.setFont(new Font("Segoe UI", Font.PLAIN, 12));
      txtGrafWidth.setMaximumSize(new Dimension(100, 30));
      row2.add(txtGrafWidth);
      row2.add(Box.createHorizontalStrut(10));

      JButton bDownload = crearBoton("Descargar Imagen", new Color(34, 197, 94));
      bDownload.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
      bDownload.addActionListener(e -> descargarGrafica(txtGrafWidth.getText()));
      row2.add(bDownload);
      row2.add(Box.createHorizontalGlue());

      lblGrafInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
      lblGrafInfo.setForeground(new Color(90, 98, 106));
      row2.add(lblGrafInfo);

      top.add(row1);
      top.add(Box.createVerticalStrut(8));
      top.add(row2);

      panelGraficas.setOpaque(false);
      body.add(top, BorderLayout.NORTH);
      body.add(panelGraficas, BorderLayout.CENTER);
      p.add(body, BorderLayout.CENTER);
      return p;
    }

    private void mostrarGraficasEnPantalla() {
      panelGraficas.removeAll();

      List<Estudiante> base = filtrarParaGraficas();
      lblGrafInfo.setText("Estudiantes: " + base.size());

      if (base.isEmpty()) {
        JLabel l = new JLabel("No hay estudiantes para graficar.");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        panelGraficas.add(l, BorderLayout.CENTER);
      } else {
        JPanel charts = new JPanel(new GridLayout(2, 1, 20, 20));
        charts.setOpaque(false);
        charts.add(new PanelBarrasRiesgo(base));
        charts.add(new PanelBarrasGrados(base));
        panelGraficas.add(charts, BorderLayout.CENTER);
      }

      panelGraficas.revalidate();
      panelGraficas.repaint();
      navigateTo("GRAF");
    }

    private void actualizarCombosGrado() {
      List<String> grados = estudiantes.stream()
          .map(Estudiante::getGrado)
          .filter(Objects::nonNull)
          .distinct()
          .sorted((g1, g2) -> {
            int p1 = obtenerPesoGrado(g1);
            int p2 = obtenerPesoGrado(g2);
            if (p1 != p2)
              return Integer.compare(p1, p2);
            return g1.compareToIgnoreCase(g2);
          })
          .collect(Collectors.toList());

      cbFiltroGrado.removeAllItems();
      cbFiltroGrado.addItem("Todos");
      grados.forEach(cbFiltroGrado::addItem);

      cbGrafGrado.removeAllItems();
      cbGrafGrado.addItem("Todos");
      grados.forEach(cbGrafGrado::addItem);
    }

    private List<Estudiante> filtrarParaGraficas() {
      String grado = (String) cbGrafGrado.getSelectedItem();
      String riesgo = (String) cbGrafRiesgo.getSelectedItem();
      String cursoStr = txtGrafCurso.getText() == null ? "" : txtGrafCurso.getText().trim();
      Integer curso = null;
      if (!cursoStr.isEmpty()) {
        try {
          curso = Integer.parseInt(cursoStr);
        } catch (Exception e) {
          JOptionPane.showMessageDialog(this, "Curso invalido.", "Error", JOptionPane.ERROR_MESSAGE);
          return List.of();
        }
      }
      Integer cursoFinal = curso;
      return estudiantes.stream()
          .filter(e -> grado == null || "Todos".equals(grado) || e.getGrado().equalsIgnoreCase(grado))
          .filter(e -> cursoFinal == null || e.getCurso() == cursoFinal)
          .filter(e -> riesgo == null || "Todos".equals(riesgo) || e.getNivelRiesgo().equalsIgnoreCase(riesgo))
          .toList();
    }

    private JPanel crearPanelBase(String titulo) {
      JPanel p = new JPanel(new BorderLayout());
      p.setBackground(BG);
      p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

      // Simple header just with the panel title
      JPanel header = new JPanel(new BorderLayout());
      header.setBackground(BG);

      JLabel t = new JLabel(titulo);
      t.setFont(new Font("Segoe UI", Font.BOLD, 18));
      t.setForeground(new Color(41, 128, 185));
      t.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      header.add(t, BorderLayout.WEST);

      p.add(header, BorderLayout.NORTH);
      return p;
    }

    private JPanel crearPanelHome() {
      JPanel p = crearPanelBase("Bienvenido");
      JPanel body = new JPanel();
      body.setBackground(BG);
      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
      body.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

      JLabel l1 = new JLabel("👋 Bienvenido al Sistema de Gestión Académica");
      l1.setFont(new Font("Segoe UI", Font.BOLD, 20));
      l1.setForeground(new Color(41, 128, 185));
      l1.setAlignmentX(JComponent.LEFT_ALIGNMENT);

      body.add(l1);
      body.add(Box.createVerticalStrut(20));

      JLabel l2 = new JLabel("Use el menú izquierdo para navegar por las diferentes secciones del sistema:");
      l2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
      l2.setForeground(new Color(52, 73, 94));
      l2.setAlignmentX(JComponent.LEFT_ALIGNMENT);
      body.add(l2);
      body.add(Box.createVerticalStrut(25));

      String[] items = {
          "📊 Estadísticas y reportes",
          "👨‍🏫 Gestión de profesores",
          "📚 Asignación de materias",
          "👦 Registro y edición de estudiantes",
          "📈 Gráficas y análisis de riesgo"
      };

      for (String item : items) {
        JLabel label = new JLabel("• " + item);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(75, 85, 99));
        label.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        body.add(label);
        body.add(Box.createVerticalStrut(8));
      }

      p.add(body, BorderLayout.CENTER);
      return p;
    }

    private JPanel crearPanelMenuProfesor() {
      JPanel p = crearPanelBase("Profesor");
      JPanel grid = new JPanel(new GridLayout(0, 1, 10, 10));
      grid.setOpaque(false);

      JButton b1 = crearBoton("Ver cursos asignados", BTN);
      JButton b2 = crearBoton("Registrar notas por materia", BTN);
      JButton b3 = crearBoton("Registrar asistencia", BTN);
      JButton b4 = crearBoton("Ver estudiantes de curso", BTN2);
      JButton b5 = crearBoton("Buscar Estudiante", BTN2);
      JButton b6 = crearBoton("Editar Notas por ID", BTN2);
      JButton b7 = crearBoton("Volver", BTN2);

      b1.addActionListener(e -> {
        actualizarTablaCursosProfesor();
        navigateTo("PROF_CURSOS");
      });
      b2.addActionListener(e -> {
        prepararPanelProfesorNotas();
        navigateTo("PROF_NOTAS");
      });
      b3.addActionListener(e -> {
        prepararPanelProfesorAsistencia();
        navigateTo("PROF_ASIS");
      });
      b4.addActionListener(e -> {
        prepararPanelProfesorVerEstudiantes();
        navigateTo("PROF_EST");
      });
      b5.addActionListener(e -> navigateTo("DOC_BUSCAR"));
      b6.addActionListener(e -> navigateTo("DOC_EDIT_NOTAS"));
      b7.addActionListener(e -> goBack());

      for (JButton b : new JButton[] { b1, b2, b3, b4, b5, b6, b7 }) {
        grid.add(b);
      }
      p.add(grid, BorderLayout.CENTER);
      return p;
    }

    private JPanel crearPanelProfesorCursos() {
      JPanel p = crearPanelBase("Cursos asignados");
      JPanel body = new JPanel(new BorderLayout());
      body.setOpaque(false);

      tablaCursosProf.setModel(modeloCursosProf);
      tablaCursosProf.setRowHeight(24);
      tablaCursosProf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
      tablaCursosProf.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

      JButton back = crearBoton("Volver", BTN2);
      back.addActionListener(e -> goBack());

      body.add(new JScrollPane(tablaCursosProf), BorderLayout.CENTER);
      body.add(back, BorderLayout.SOUTH);
      p.add(body, BorderLayout.CENTER);
      return p;
    }

    private JPanel crearPanelProfesorNotas() {
      JButton submit = crearBoton("Registrar", BTN);
      JButton back = crearBoton("Volver", BTN2);
      submit.addActionListener(e -> accionProfesorRegistrarNotas());
      back.addActionListener(e -> goBack());

      return crearFormulario("Registrar notas (Materia)",
          campoLabeled("Curso", cbProfCursoNotas),
          campoLabeled("Materia", cbProfMateriaNotas),
          campoLabeled("Corte", fProfCorte),
          campoLabeled("Cantidad de notas", fProfCantNotas),
          botonesRow(submit, back));
    }

    private JPanel crearPanelProfesorAsistencia() {
      JButton submit = crearBoton("Registrar", BTN);
      JButton back = crearBoton("Volver", BTN2);
      submit.addActionListener(e -> accionProfesorRegistrarAsistencia());
      back.addActionListener(e -> goBack());

      return crearFormulario("Registrar asistencia",
          campoLabeled("Curso", cbProfCursoAsis),
          campoLabeled("Mes", cbProfMes),
          botonesRow(submit, back));
    }

    private JPanel crearPanelProfesorVerEstudiantes() {
      JButton ver = crearBoton("Mostrar", BTN);
      JButton back = crearBoton("Volver", BTN2);
      ver.addActionListener(e -> accionProfesorVerEstudiantes());
      back.addActionListener(e -> goBack());

      return crearFormulario("Ver estudiantes",
          campoLabeled("Curso", cbProfCursoVer),
          botonesRow(ver, back));
    }

    private void actualizarTablaCursosProfesor() {
      modeloCursosProf.setColumnIdentifiers(new String[] { "Grado", "Curso", "Materias" });
      modeloCursosProf.setRowCount(0);
      if (profesorActual == null) {
        return;
      }
      for (CursoAcademico c : cursosAsignadosProfesorOrdenados()) {
        Set<String> mats = asignaciones.stream()
            .filter(a -> a != null && profesorActual.usuario.equals(a.usuarioProfesor))
            .filter(a -> a.cursoKey().equals(c))
            .map(a -> a.materia)
            .filter(m -> m != null && !m.trim().isEmpty())
            .collect(Collectors.toSet());
        String materias = mats.stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.joining(", "));
        modeloCursosProf.addRow(new Object[] { c.grado, c.curso, materias });
      }
    }

    private void accionProfesorRegistrarNotas() {
      CursoAcademico curso = parseCursoDisplay((String) cbProfCursoNotas.getSelectedItem());
      if (curso == null) {
        JOptionPane.showMessageDialog(this, "Seleccione un curso.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      String materia = (String) cbProfMateriaNotas.getSelectedItem();
      if (materia == null || materia.trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Seleccione una materia.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      Integer corte = parseIntField(fProfCorte, "Corte invalido");
      Integer cant = parseIntField(fProfCantNotas, "Cantidad invalida");
      if (corte == null || cant == null)
        return;
      if (corte < 1 || corte > 3) {
        JOptionPane.showMessageDialog(this, "Corte invalido (1-3).", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      if (cant <= 0) {
        JOptionPane.showMessageDialog(this, "Cantidad de notas debe ser > 0.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      List<Estudiante> cursoList = estudiantes.stream()
          .filter(e -> CursoAcademico.normalizarGrado(e.getGrado()).equals(CursoAcademico.normalizarGrado(curso.grado))
              && e.getCurso() == curso.curso)
          .toList();
      if (cursoList.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No hay estudiantes en ese curso.", "Info",
            JOptionPane.INFORMATION_MESSAGE);
        return;
      }

      for (Estudiante e : cursoList) {
        List<Double> nuevas = new ArrayList<>();
        for (int i = 0; i < cant; i++) {
          String v = JOptionPane.showInputDialog(this,
              "Nota " + (i + 1) + " (1.0-5.0) para " + e.getNombre() + " (" + materia + "):");
          if (v == null)
            return;
          try {
            double n = Double.parseDouble(v.trim());
            if (n < 1.0 || n > 5.0) {
              JOptionPane.showMessageDialog(this, "Nota invalida (1.0-5.0).", "Error", JOptionPane.ERROR_MESSAGE);
              return;
            }
            nuevas.add(n);
          } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Nota invalida.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
          }
        }
        e.agregarNotasMateria(materia.trim(), corte, nuevas);
      }

      guardarDatos();
      refrescarTabla(estudiantesVisibles());
      JOptionPane.showMessageDialog(this, "Notas registradas.", "OK", JOptionPane.INFORMATION_MESSAGE);
    }

    private void accionProfesorRegistrarAsistencia() {
      CursoAcademico curso = parseCursoDisplay((String) cbProfCursoAsis.getSelectedItem());
      if (curso == null) {
        JOptionPane.showMessageDialog(this, "Seleccione un curso.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      Integer mes = parseMesDisplay((String) cbProfMes.getSelectedItem());
      if (mes == null) {
        JOptionPane.showMessageDialog(this, "Seleccione un mes.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      List<Estudiante> cursoList = estudiantes.stream()
          .filter(e -> CursoAcademico.normalizarGrado(e.getGrado()).equals(CursoAcademico.normalizarGrado(curso.grado))
              && e.getCurso() == curso.curso)
          .toList();
      if (cursoList.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No hay estudiantes en ese curso.", "Info",
            JOptionPane.INFORMATION_MESSAGE);
        return;
      }
      for (Estudiante e : cursoList) {
        String v = JOptionPane.showInputDialog(this, "Faltas del mes (0-20) para " + e.getNombre() + ":");
        if (v == null)
          return;
        try {
          int faltas = Integer.parseInt(v.trim());
          if (faltas < 0 || faltas > 20) {
            JOptionPane.showMessageDialog(this, "Faltas fuera de rango.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
          }
          e.agregarFaltasMes(mes, faltas);
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(this, "Valor invalido.", "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
      }
      guardarDatos();
      refrescarTabla(estudiantesVisibles());
      JOptionPane.showMessageDialog(this, "Asistencia registrada.", "OK", JOptionPane.INFORMATION_MESSAGE);
    }

    private void accionProfesorVerEstudiantes() {
      CursoAcademico curso = parseCursoDisplay((String) cbProfCursoVer.getSelectedItem());
      if (curso == null) {
        JOptionPane.showMessageDialog(this, "Seleccione un curso.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      List<Estudiante> cursoList = estudiantesVisibles().stream()
          .filter(e -> CursoAcademico.normalizarGrado(e.getGrado()).equals(CursoAcademico.normalizarGrado(curso.grado))
              && e.getCurso() == curso.curso)
          .toList();
      refrescarTabla(cursoList);
    }

    private JPanel crearPanelMenuPsico() {
      JPanel p = crearPanelBase("Psicosocial");
      JPanel grid = new JPanel(new GridLayout(0, 1, 10, 10));
      grid.setOpaque(false);

      JButton b1 = crearBoton("Ver Alerta/Riesgo", BTN);
      JButton b2 = crearBoton("Enviar Alerta Acudiente", new Color(46, 204, 113));
      JButton b3 = crearBoton("Exportar Riesgo CSV", BTN2);
      JButton b4 = crearBoton("Volver a Inicio", BTN2);

      b1.addActionListener(e -> {
        List<Estudiante> riesgo = estudiantes.stream()
            .filter(es -> !es.getNivelRiesgo().equals("NORMAL"))
            .toList();
        if (riesgo.isEmpty()) {
          JOptionPane.showMessageDialog(this,
              "No hay estudiantes con alertas académicas o de asistencia.", "Info",
              JOptionPane.INFORMATION_MESSAGE);
        }
        refrescarTabla(riesgo);
      });
      b2.addActionListener(e -> {
        int row = tabla.getSelectedRow();
        if (row < 0) {
          JOptionPane.showMessageDialog(this, "Seleccione un estudiante en riesgo de la tabla.", "Aviso",
              JOptionPane.WARNING_MESSAGE);
          return;
        }
        int id = (int) tabla.getValueAt(row, 0);
        buscarPorId(id).ifPresent(this::prepararAlertaAcudiente);
      });
      b3.addActionListener(e -> navigateTo("PSICO_EXPORT"));
      b4.addActionListener(e -> goBack());

      for (JButton b : new JButton[] { b1, b2, b3, b4 }) {
        grid.add(b);
      }
      p.add(grid, BorderLayout.CENTER);
      return p;
    }

    private void prepararAlertaAcudiente(Estudiante e) {
      double[] promedios = new double[3];
      for (int i = 1; i <= 3; i++) {
        promedios[i - 1] = e.getPromedioCorte(i);
      }
      enviarAlertaAcudiente(e.getCorreoAcudiente(), e.getNombre(), e.getGrado(), e.getTotalFaltas(), promedios);
    }

    private void enviarAlertaAcudiente(String correo, String nombreEst, String grado, int inasistencias,
        double[] notas) {
      StringBuilder sb = new StringBuilder();
      sb.append("Asunto: Alerta de Riesgo de Deserción Escolar - ").append(nombreEst).append("\n\n");
      sb.append("Estimado acudiente (").append(correo).append("):\n\n");
      sb.append("Le informamos que el estudiante ").append(nombreEst).append(" de grado ").append(grado)
          .append(" se encuentra actualmente en riesgo de deserción.\n\n");
      sb.append(
          "Esta situación se ha detectado tras el último seguimiento académico, debido a que el alumno está presentando inasistencias constantes y mantiene un promedio de notas bajas en los cortes registrados hasta la fecha.\n\n");
      sb.append("A continuación, se detalla el reporte de apoyo:\n");
      sb.append(" * Número de inasistencias acumuladas: ").append(inasistencias).append("\n");
      sb.append(" * Calificaciones por cortes registrados:\n");

      for (int i = 0; i < notas.length; i++) {
        sb.append("    * Corte ").append(i + 1).append(": ").append(String.format("%.2f", notas[i])).append("\n");
      }

      sb.append(
          "\nLe solicitamos encarecidamente comunicarse con la institución o asistir a una cita con el cuerpo docente para revisar este caso y tomar las medidas necesarias para evitar la pérdida del cupo escolar. Alguna duda avervarce a nuestras instalaciones\n\n");
      sb.append("Atentamente,\n");
      sb.append("Orientación psicosocial y directivos");

      JTextArea textArea = new JTextArea(15, 45);
      textArea.setText(sb.toString());
      textArea.setEditable(false);
      textArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);

      JScrollPane scroll = new JScrollPane(textArea);
      scroll.setBorder(BorderFactory.createTitledBorder("Previsualización de Notificación"));

      JPanel content = new JPanel(new BorderLayout(10, 10));
      content.add(scroll, BorderLayout.CENTER);

      JButton btnCopy = crearBoton("Copiar al Portapapeles", BTN);
      btnCopy.addActionListener(ev -> {
        textArea.selectAll();
        textArea.copy();
        JOptionPane.showMessageDialog(this, "Texto copiado al portapapeles!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
      });
      content.add(btnCopy, BorderLayout.SOUTH);

      JOptionPane.showMessageDialog(this, content, "Alerta Psicosocial", JOptionPane.PLAIN_MESSAGE);
    }

    private JPanel crearPanelMenuDirectiva() {
      JPanel p = crearPanelBase("Directiva");
      JPanel grid = new JPanel(new GridLayout(0, 1, 10, 10));
      grid.setOpaque(false);

      JButton b1 = crearBoton("Estadisticas", BTN);
      JButton b2 = crearBoton("Gestionar Profesores", BTN);
      JButton b3 = crearBoton("Asignar Materias", BTN);
      JButton b4 = crearBoton("Registrar Estudiante", BTN2);
      JButton b5 = crearBoton("Editar Estudiante", BTN2);
      JButton b6 = crearBoton("Eliminar Estudiante", new Color(231, 76, 60));
      JButton b7 = crearBoton("Buscar Estudiante", BTN2);
      JButton b8 = crearBoton("Volver a Inicio", BTN2);

      b1.addActionListener(e -> {
        actualizarStats();
        navigateTo("DIR_STATS");
      });
      b2.addActionListener(e -> {
        prepararPanelDirectivaProfesores();
        navigateTo("DIR_PROF");
      });
      b3.addActionListener(e -> {
        prepararPanelDirectivaAsignaciones();
        navigateTo("DIR_ASIG");
      });
      b4.addActionListener(e -> navigateTo("DOC_REG"));
      b5.addActionListener(e -> navigateTo("DOC_EDIT_EST"));
      b6.addActionListener(e -> navigateTo("DOC_ELIM"));
      b7.addActionListener(e -> navigateTo("DOC_BUSCAR"));
      b8.addActionListener(e -> goBack());

      for (JButton b : new JButton[] { b1, b2, b3, b4, b5, b6, b7, b8 }) {
        grid.add(b);
      }
      p.add(grid, BorderLayout.CENTER);
      return p;
    }

    private JPanel crearPanelGestionProfesores() {
      JButton submit = crearBoton("Crear Profesor", BTN);
      JButton back = crearBoton("Volver", BTN2);
      submit.addActionListener(e -> accionCrearProfesor());
      back.addActionListener(e -> goBack());

      return crearFormulario("Gestion de Profesores",
          campoLabeled("Usuario", fDirProfUser),
          campoLabeled("Contraseña", fDirProfPass),
          campoLabeled("Nombre", fDirProfNombre),
          botonesRow(submit, back));
    }

    private JPanel crearPanelAsignarMaterias() {
      JButton submit = crearBoton("Asignar", BTN);
      JButton back = crearBoton("Volver", BTN2);
      submit.addActionListener(e -> accionAsignarMateria());
      back.addActionListener(e -> goBack());

      return crearFormulario("Asignar Materias",
          campoLabeled("Grado", fDirAsigGrado),
          campoLabeled("Curso", fDirAsigCurso),
          campoLabeled("Materia", cbDirAsigMateria),
          campoLabeled("Profesor", cbDirAsigProfesor),
          botonesRow(submit, back));
    }

    private void prepararPanelDirectivaProfesores() {
      fDirProfUser.setText("");
      fDirProfPass.setText("");
      fDirProfNombre.setText("");
    }

    private void prepararPanelDirectivaAsignaciones() {
      fDirAsigGrado.setText("");
      fDirAsigCurso.setText("");
      cbDirAsigMateria.removeAllItems();
      oficial.MATERIAS_BASICAS.forEach(cbDirAsigMateria::addItem);
      cbDirAsigProfesor.removeAllItems();
      profesores.stream()
          .map(p -> p.usuario)
          .sorted(String.CASE_INSENSITIVE_ORDER)
          .forEach(cbDirAsigProfesor::addItem);
    }

    private void accionCrearProfesor() {
      String user = fDirProfUser.getText().trim();
      String pass = fDirProfPass.getText().trim();
      String nombre = fDirProfNombre.getText().trim();
      if (user.isEmpty() || pass.isEmpty() || nombre.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Complete todos los campos.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      if (user.contains("|") || pass.contains("|") || nombre.contains("|")) {
        JOptionPane.showMessageDialog(this, "Caracter '|' no permitido.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      boolean existe = profesores.stream().anyMatch(p -> p.usuario.equalsIgnoreCase(user));
      if (existe) {
        JOptionPane.showMessageDialog(this, "Usuario de profesor ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      profesores.add(new Profesor(user, pass, nombre));
      guardarProfesores();
      JOptionPane.showMessageDialog(this, "Profesor creado.", "OK", JOptionPane.INFORMATION_MESSAGE);
    }

    private void accionAsignarMateria() {
      String grado = fDirAsigGrado.getText().trim();
      Integer curso = parseIntField(fDirAsigCurso, "Curso invalido");
      String materia = (String) cbDirAsigMateria.getSelectedItem();
      String profUser = (String) cbDirAsigProfesor.getSelectedItem();
      if (grado.isEmpty() || curso == null || materia == null || materia.trim().isEmpty() || profUser == null
          || profUser.trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Complete todos los campos.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      if (materia.contains("|") || materia.contains("@") || materia.contains("§")) {
        JOptionPane.showMessageDialog(this, "Caracteres no permitidos en materia: | @ §", "Error",
            JOptionPane.ERROR_MESSAGE);
        return;
      }

      String gradoN = CursoAcademico.normalizarGrado(grado);
      String materiaCanon = oficial.MATERIAS_BASICAS.stream()
          .filter(m -> Estudiante.normalizarTexto(m).equals(Estudiante.normalizarTexto(materia)))
          .findFirst()
          .orElse(materia.trim());
      String materiaN = Estudiante.normalizarTexto(materiaCanon);

      // Check for existing assignment
      Optional<Asignacion> existingAsignacion = asignaciones.stream()
          .filter(a -> a != null
              && CursoAcademico.normalizarGrado(a.grado).equals(gradoN)
              && a.curso == curso
              && a.materia != null
              && Estudiante.normalizarTexto(a.materia).equals(materiaN))
          .findFirst();

      if (existingAsignacion.isPresent()) {
        // Show confirmation dialog
        int option = JOptionPane.showConfirmDialog(
            this,
            "La materia " + materiaCanon + " ya está asignada al profesor " + existingAsignacion.get().usuarioProfesor +
                " para el grado " + grado + " y curso " + curso + ".\n¿Desea reemplazar al profesor actual?",
            "Asignación Duplicada",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (option != JOptionPane.YES_OPTION) {
          return; // Cancel
        }
        // Remove existing assignment
        asignaciones.remove(existingAsignacion.get());
      }

      asignaciones.add(new Asignacion(grado.trim(), curso, materiaCanon, profUser.trim()));
      guardarAsignaciones();
      JOptionPane.showMessageDialog(this, "Asignacion guardada.", "OK", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel crearFormulario(String titulo, JComponent... campos) {
      JPanel wrap = crearPanelBase(titulo);
      JPanel body = new JPanel();
      body.setOpaque(false);
      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
      body.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

      for (JComponent c : campos) {
        body.add(c);
        body.add(Box.createVerticalStrut(15));
      }
      wrap.add(body, BorderLayout.CENTER);
      return wrap;
    }

    private JPanel campoLabeled(String label, JComponent comp) {
      JPanel p = new JPanel(new BorderLayout());
      p.setOpaque(false);
      p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      JLabel l = new JLabel(label);
      l.setFont(new Font("Segoe UI", Font.BOLD, 13));
      l.setForeground(new Color(52, 73, 94));

      comp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
      comp.setPreferredSize(new Dimension(300, 38));
      comp.setMaximumSize(new Dimension(400, 38));

      styleInput(comp);

      p.add(l, BorderLayout.NORTH);
      p.add(Box.createVerticalStrut(8), BorderLayout.CENTER);
      p.add(comp, BorderLayout.SOUTH);
      return p;
    }

    private JPanel crearPanelRegistrarEstudiante() {
      JButton submit = crearBoton("Guardar Estudiante", BTN);
      JButton back = crearBoton("Volver", BTN2);
      submit.addActionListener(e -> accionRegistrarEstudiante());
      back.addActionListener(e -> goBack());

      // Initialize materia combo box
      cbRegMateria.removeAllItems();
      oficial.MATERIAS_BASICAS.forEach(cbRegMateria::addItem);

      return crearFormulario("Registrar Estudiante",
          campoLabeled("ID", fRegId),
          campoLabeled("Nombre", fRegNombre),
          campoLabeled("Edad (3-100)", fRegEdad),
          campoLabeled("Grado (1-11, Transicion, Jardin)", fRegGrado),
          campoLabeled("Curso", fRegCurso),
          campoLabeled("Materia", cbRegMateria),
          campoLabeled("Nombre Acudiente", fRegAcud),
          campoLabeled("Correo Acudiente", fRegCorreo),
          campoLabeled("Telefono Acudiente", fRegTel),
          botonesRow(submit, back));
    }

    private JPanel crearPanelNotasMasivas() {
      JButton submit = crearBoton("Registrar Notas", BTN);
      JButton back = crearBoton("Volver", BTN2);
      submit.addActionListener(e -> accionNotasMasivas());
      back.addActionListener(e -> goBack());
      return crearFormulario("Notas Masivas (Curso)",
          campoLabeled("Grado", fCursoGrado),
          campoLabeled("Curso", fCursoCurso),
          campoLabeled("Corte", fCursoCorte),
          campoLabeled("Cantidad de Notas", fCursoCantNotas),
          botonesRow(submit, back));
    }

    private JPanel crearPanelAsistenciaMasiva() {
      JButton submit = crearBoton("Registrar Asistencia", BTN);
      JButton back = crearBoton("Volver", BTN2);
      submit.addActionListener(e -> accionAsistenciaMasiva());
      back.addActionListener(e -> goBack());
      return crearFormulario("Asistencia Masiva (Curso)",
          campoLabeled("Grado", fAsisGrado),
          campoLabeled("Curso", fAsisCurso),
          campoLabeled("Max dias (1-5)", fAsisDiasMax),
          botonesRow(submit, back));
    }

    private JPanel crearPanelBuscar() {
      JButton submit = crearBoton("Buscar", BTN);
      JButton back = crearBoton("Volver", BTN2);
      submit.addActionListener(e -> accionBuscar());
      back.addActionListener(e -> goBack());
      return crearFormulario("Buscar Estudiante",
          campoLabeled("Tipo", cbBuscarTipo),
          campoLabeled("Texto", fBuscarQuery),
          botonesRow(submit, back));
    }

    private JPanel crearPanelEditarNotas() {
      JButton submit = crearBoton("Actualizar Nota", BTN);
      JButton back = crearBoton("Volver", BTN2);
      submit.addActionListener(e -> accionEditarNota());
      back.addActionListener(e -> goBack());
      return crearFormulario("Editar Nota (por ID)",
          campoLabeled("ID Estudiante", fEditNotasId),
          campoLabeled("Corte", fEditNotasCorte),
          campoLabeled("Indice (0..n)", fEditNotasIndice),
          campoLabeled("Nueva Nota (1.0-5.0)", fEditNotasNueva),
          botonesRow(submit, back));
    }

    private JPanel crearPanelEditarEstudiante() {
      JButton cargar = crearBoton("Cargar por ID", BTN2);
      JButton guardar = crearBoton("Guardar Cambios", BTN);
      JButton back = crearBoton("Volver", BTN2);
      cargar.addActionListener(e -> accionCargarEdicion());
      guardar.addActionListener(e -> accionGuardarEdicion());
      back.addActionListener(e -> goBack());
      return crearFormulario("Editar Estudiante",
          campoLabeled("ID", fEditEstId),
          botonesRow(cargar),
          campoLabeled("Nombre", fEditEstNombre),
          campoLabeled("Grado", fEditEstGrado),
          campoLabeled("Curso", fEditEstCurso),
          campoLabeled("Acudiente", fEditEstAcud),
          campoLabeled("Correo", fEditEstCorreo),
          campoLabeled("Telefono", fEditEstTel),
          botonesRow(guardar, back));
    }

    private JPanel crearPanelEliminar() {
      JButton submit = crearBoton("Eliminar", new Color(231, 76, 60));
      JButton back = crearBoton("Volver", BTN2);
      submit.addActionListener(e -> accionEliminar());
      back.addActionListener(e -> goBack());
      return crearFormulario("Eliminar Estudiante",
          campoLabeled("ID", fEliminarId),
          botonesRow(submit, back));
    }

    private JPanel crearPanelStats() {
      JPanel p = crearPanelBase("Estadisticas");
      lblStats.setFont(new Font("Segoe UI", Font.PLAIN, 13));
      p.add(lblStats, BorderLayout.CENTER);
      return p;
    }

    private JPanel crearPanelExportarRiesgo() {
      JButton export = crearBoton("Exportar Riesgo", BTN);
      JButton back = crearBoton("Volver", BTN2);
      export.addActionListener(e -> accionExportarRiesgo());
      back.addActionListener(e -> goBack());
      return crearFormulario("Exportar CSV Riesgo",
          new JLabel("Genera reporte de ALERTA y RIESGO de desercion."),
          botonesRow(export, back));
    }

    private JPanel botonesRow(JButton... buttons) {
      JPanel p = new JPanel();
      p.setOpaque(false);
      p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
      p.setAlignmentX(JComponent.LEFT_ALIGNMENT);

      for (int i = 0; i < buttons.length; i++) {
        JButton b = buttons[i];
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        p.add(b);
        if (i < buttons.length - 1) {
          p.add(Box.createHorizontalStrut(12));
        }
      }
      return p;
    }

    private void accionRegistrarEstudiante() {
      Integer id = parseIntField(fRegId, "ID invalido");
      if (id == null)
        return;
      if (oficial.buscarPorId(id).isPresent()) {
        JOptionPane.showMessageDialog(this, "ID ya registrado.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      String nombre = fRegNombre.getText().trim();
      Integer edad = parseIntField(fRegEdad, "Edad invalida");
      String grado = fRegGrado.getText().trim();
      Integer curso = parseIntField(fRegCurso, "Curso invalido");
      if (nombre.isEmpty() || grado.isEmpty() || edad == null || curso == null)
        return;
      if (edad < 3 || edad > 100) {
        JOptionPane.showMessageDialog(this, "Edad invalida (3-100).", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      String acud = fRegAcud.getText().trim();
      String correo = fRegCorreo.getText().trim();
      String tel = fRegTel.getText().trim();
      if (acud.isEmpty() || correo.isEmpty() || tel.isEmpty())
        return;

      estudiantes.add(new Estudiante(id, nombre, edad, grado, curso, acud, correo, tel));
      guardarDatos();
      actualizarCombosGrado();
      refrescarTabla(estudiantes);
    }

    private void accionNotasMasivas() {
      String grado = fCursoGrado.getText().trim();
      Integer curso = parseIntField(fCursoCurso, "Curso invalido");
      Integer corte = parseIntField(fCursoCorte, "Corte invalido");
      Integer cant = parseIntField(fCursoCantNotas, "Cantidad invalida");
      if (grado.isEmpty() || curso == null || corte == null || cant == null)
        return;
      if (corte < 1 || corte > 3) {
        JOptionPane.showMessageDialog(this, "Corte invalido (1-3).", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      if (cant <= 0) {
        JOptionPane.showMessageDialog(this, "Cantidad de notas debe ser > 0.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      List<Estudiante> cursoList = estudiantes.stream()
          .filter(e -> e.getGrado().equalsIgnoreCase(grado) && e.getCurso() == curso)
          .toList();
      if (cursoList.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No hay estudiantes en ese curso.", "Info",
            JOptionPane.INFORMATION_MESSAGE);
        return;
      }

      for (Estudiante e : cursoList) {
        List<Double> nuevas = new ArrayList<>();
        for (int i = 0; i < cant; i++) {
          String v = JOptionPane.showInputDialog(this, "Nota " + (i + 1) + " (1.0-5.0) para " + e.getNombre() + ":");
          if (v == null)
            return;
          try {
            double n = Double.parseDouble(v.trim());
            if (n < 1.0 || n > 5.0) {
              JOptionPane.showMessageDialog(this, "Nota invalida (1.0-5.0).", "Error", JOptionPane.ERROR_MESSAGE);
              return;
            }
            nuevas.add(n);
          } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Nota invalida.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
          }
        }
        e.agregarNotas(corte, nuevas);
      }
      guardarDatos();
      refrescarTabla(estudiantes);
    }

    private void accionAsistenciaMasiva() {
      String grado = fAsisGrado.getText().trim();
      Integer curso = parseIntField(fAsisCurso, "Curso invalido");
      Integer diasMax = parseIntField(fAsisDiasMax, "Max dias invalido");
      if (grado.isEmpty() || curso == null || diasMax == null)
        return;
      if (diasMax <= 0 || diasMax > 5) {
        JOptionPane.showMessageDialog(this, "El maximo permitido son 5 dias.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      List<Estudiante> cursoList = estudiantes.stream()
          .filter(e -> e.getGrado().equalsIgnoreCase(grado) && e.getCurso() == curso)
          .toList();
      if (cursoList.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No hay estudiantes en ese curso.", "Info",
            JOptionPane.INFORMATION_MESSAGE);
        return;
      }
      for (Estudiante e : cursoList) {
        String v = JOptionPane.showInputDialog(this, "Faltas (0-" + diasMax + ") para " + e.getNombre() + ":");
        if (v == null)
          return;
        try {
          int faltas = Integer.parseInt(v.trim());
          if (faltas < 0 || faltas > diasMax) {
            JOptionPane.showMessageDialog(this, "Faltas fuera de rango.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
          }
          e.agregarFaltas(faltas);
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(this, "Valor invalido.", "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
      }
      guardarDatos();
      refrescarTabla(estudiantes);
    }

    private void accionBuscar() {
      String tipo = (String) cbBuscarTipo.getSelectedItem();
      String q = fBuscarQuery.getText().trim();
      if (q.isEmpty())
        return;
      if ("ID".equals(tipo)) {
        try {
          int id = Integer.parseInt(q);
          Optional<Estudiante> e = oficial.buscarPorId(id);
          if (e.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No encontrado.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
          }
          refrescarTabla(List.of(e.get()));
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(this, "ID invalido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
      } else {
        String s = q.toLowerCase();
        List<Estudiante> res = estudiantes.stream()
            .filter(es -> es.getNombre().toLowerCase().contains(s))
            .toList();
        refrescarTabla(res);
      }
    }

    private void accionEditarNota() {
      Integer id = parseIntField(fEditNotasId, "ID invalido");
      Integer corte = parseIntField(fEditNotasCorte, "Corte invalido");
      Integer idx = parseIntField(fEditNotasIndice, "Indice invalido");
      Double nueva = parseDoubleField(fEditNotasNueva, "Nota invalida");
      if (id == null || corte == null || idx == null || nueva == null)
        return;
      if (corte < 1 || corte > 3) {
        JOptionPane.showMessageDialog(this, "Corte invalido (1-3).", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      if (nueva < 1.0 || nueva > 5.0) {
        JOptionPane.showMessageDialog(this, "Nota invalida (1.0-5.0).", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      Optional<Estudiante> opt = oficial.buscarPorId(id);
      if (opt.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      Estudiante e = opt.get();
      if (!e.getNotasPorCorte().containsKey(corte)) {
        JOptionPane.showMessageDialog(this, "Corte no existe.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      if (idx < 0 || idx >= e.getNotasPorCorte().get(corte).size()) {
        JOptionPane.showMessageDialog(this, "Indice fuera de rango.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      e.editarNota(corte, idx, nueva);
      guardarDatos();
      refrescarTabla(estudiantes);
    }

    private void accionCargarEdicion() {
      Integer id = parseIntField(fEditEstId, "ID invalido");
      if (id == null)
        return;
      Optional<Estudiante> opt = oficial.buscarPorId(id);
      if (opt.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      Estudiante e = opt.get();
      fEditEstNombre.setText(e.getNombre());
      fEditEstGrado.setText(e.getGrado());
      fEditEstCurso.setText(String.valueOf(e.getCurso()));
      fEditEstAcud.setText(e.getNombreAcudiente());
      fEditEstCorreo.setText(e.getCorreoAcudiente());
      fEditEstTel.setText(e.getTelefonoAcudiente());
    }

    private void accionGuardarEdicion() {
      Integer id = parseIntField(fEditEstId, "ID invalido");
      if (id == null)
        return;
      Optional<Estudiante> opt = oficial.buscarPorId(id);
      if (opt.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      Estudiante e = opt.get();
      String nombre = fEditEstNombre.getText().trim();
      String grado = fEditEstGrado.getText().trim();
      Integer curso = parseIntField(fEditEstCurso, "Curso invalido");
      if (nombre.isEmpty() || grado.isEmpty() || curso == null)
        return;
      e.setNombre(nombre);
      e.setGrado(grado);
      e.setCurso(curso);
      e.setNombreAcudiente(fEditEstAcud.getText().trim());
      e.setCorreoAcudiente(fEditEstCorreo.getText().trim());
      e.setTelefonoAcudiente(fEditEstTel.getText().trim());
      guardarDatos();
      actualizarCombosGrado();
      refrescarTabla(estudiantes);
    }

    private void accionEliminar() {
      Integer id = parseIntField(fEliminarId, "ID invalido");
      if (id == null)
        return;
      int conf = JOptionPane.showConfirmDialog(this, "Seguro que desea eliminar el estudiante " + id + "?", "Confirmar",
          JOptionPane.YES_NO_OPTION);
      if (conf != JOptionPane.YES_OPTION)
        return;
      if (estudiantes.removeIf(e -> e.getId() == id)) {
        guardarDatos();
        actualizarCombosGrado();
        refrescarTabla(estudiantes);
      } else {
        JOptionPane.showMessageDialog(this, "No encontrado.", "Info", JOptionPane.INFORMATION_MESSAGE);
      }
    }

    private void accionExportarRiesgo() {
      List<Estudiante> riesgo = estudiantes.stream()
          .filter(es -> es.getNivelRiesgo().equals("RIESGO DE DESERCIÓN")
              || es.getNivelRiesgo().equals("ALERTA DE DESERCIÓN"))
          .toList();
      if (riesgo.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No hay estudiantes en riesgo para exportar.", "Info",
            JOptionPane.INFORMATION_MESSAGE);
        return;
      }
      String nom = "reporte_riesgo_" + institucionActual.usuario + ".csv";
      exportarGenericoCSV(riesgo, nom);
      JOptionPane.showMessageDialog(this, "Exportado: " + nom, "OK", JOptionPane.INFORMATION_MESSAGE);
    }

    private void actualizarStats() {
      long total = estudiantes.size();
      long rRiesgo = estudiantes.stream().filter(e -> e.getNivelRiesgo().equals("RIESGO DE DESERCIÓN")).count();
      long rAlerta = estudiantes.stream().filter(e -> e.getNivelRiesgo().equals("ALERTA DE DESERCIÓN")).count();
      long rNormal = estudiantes.stream().filter(e -> e.getNivelRiesgo().equals("NORMAL")).count();

      // Calculate acceleration for ALL students
      long registrosMarzoTotal = estudiantes.stream().filter(e -> e.getMesRegistro() == 3).count();
      long registrosMayoTotal = estudiantes.stream().filter(e -> e.getMesRegistro() == 5).count();
      double deltaVTotal = registrosMayoTotal - registrosMarzoTotal;
      double deltaT = 5 - 3; // 2 months
      double aceleracionTotal = deltaVTotal / deltaT;

      // Calculate acceleration for AT RISK students
      List<Estudiante> estudiantesRiesgo = estudiantes.stream()
          .filter(
              e -> e.getNivelRiesgo().equals("RIESGO DE DESERCIÓN") || e.getNivelRiesgo().equals("ALERTA DE DESERCIÓN"))
          .toList();
      long registrosMarzoRiesgo = estudiantesRiesgo.stream().filter(e -> e.getMesRegistro() == 3).count();
      long registrosMayoRiesgo = estudiantesRiesgo.stream().filter(e -> e.getMesRegistro() == 5).count();
      double deltaVRiesgo = registrosMayoRiesgo - registrosMarzoRiesgo;
      double aceleracionRiesgo = deltaVRiesgo / deltaT;

      lblStats.setText("<html>Total: " + total + "<br/>RIESGO DE DESERCIÓN: " + rRiesgo + "<br/>ALERTA DE DESERCIÓN: "
          + rAlerta + "<br/>NORMAL: " + rNormal + "<br/><br/><b>Análisis de Aceleración (Total de Estudiantes):</b>"
          + "<br/>Registros Marzo: " + registrosMarzoTotal + "<br/>Registros Mayo: " + registrosMayoTotal
          + "<br/>Aceleración: " + String.format("%.2f", aceleracionTotal) + " registros/mes"
          + "<br/><br/><b>Análisis de Aceleración (Estudiantes en Riesgo/Alerta):</b>" + "<br/>Registros Marzo: "
          + registrosMarzoRiesgo + "<br/>Registros Mayo: " + registrosMayoRiesgo + "<br/>Aceleración: "
          + String.format("%.2f", aceleracionRiesgo) + " registros/mes</html>");
    }

    private void descargarGrafica(String widthStr) {
      int width;
      try {
        width = Integer.parseInt(widthStr.trim());
        if (width <= 0)
          throw new NumberFormatException();
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Ancho inválido. Por favor, ingrese un número positivo.", "Error",
            JOptionPane.ERROR_MESSAGE);
        return;
      }

      // Create a temporary panel to render the graph with the desired width
      List<Estudiante> data = estudiantesVisibles();
      JPanel tempPanel = new JPanel(new GridLayout(1, 2, 20, 20));
      tempPanel.setPreferredSize(new Dimension(width, 400));
      tempPanel.add(new PanelBarrasRiesgo(data));
      tempPanel.add(new PanelBarrasGrados(data));
      tempPanel.setSize(tempPanel.getPreferredSize());
      tempPanel.doLayout();

      // Create image
      BufferedImage image = new BufferedImage(tempPanel.getWidth(), tempPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
      Graphics2D g2d = image.createGraphics();
      tempPanel.paint(g2d);
      g2d.dispose();

      // Save file
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setDialogTitle("Guardar gráfica");
      fileChooser.setSelectedFile(new File("grafica.png"));
      int userSelection = fileChooser.showSaveDialog(this);

      if (userSelection == JFileChooser.APPROVE_OPTION) {
        File fileToSave = fileChooser.getSelectedFile();
        String filePath = fileToSave.getAbsolutePath();
        if (!filePath.toLowerCase().endsWith(".png") && !filePath.toLowerCase().endsWith(".jpg")
            && !filePath.toLowerCase().endsWith(".jpeg")) {
          filePath += ".png";
        }
        try {
          String format = filePath.toLowerCase().endsWith(".jpg") || filePath.toLowerCase().endsWith(".jpeg") ? "JPG"
              : "PNG";
          ImageIO.write(image, format, new File(filePath));
          JOptionPane.showMessageDialog(this, "Gráfica guardada exitosamente: " + filePath, "Éxito",
              JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
          JOptionPane.showMessageDialog(this, "Error al guardar la gráfica: " + ex.getMessage(), "Error",
              JOptionPane.ERROR_MESSAGE);
        }
      }
    }

    private void mostrarDialogoCorreo() {
      JDialog dialog = new JDialog(this, "Mandar Correo a Padres", true);
      dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      dialog.setSize(700, 600);
      dialog.setLocationRelativeTo(this);
      dialog.setLayout(new BorderLayout(10, 10));

      JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
      mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
      mainPanel.setBackground(new Color(245, 247, 250));

      // Student selector
      JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
      selectorPanel.setBackground(new Color(245, 247, 250));
      selectorPanel.add(new JLabel("Seleccionar Estudiante:"));

      JComboBox<String> cbEstudiantes = new JComboBox<>();
      cbEstudiantes.setPreferredSize(new Dimension(300, 30));
      for (Estudiante e : estudiantes) {
        if (e.getNivelRiesgo().equals("RIESGO DE DESERCIÓN") || e.getNivelRiesgo().equals("ALERTA DE DESERCIÓN")) {
          cbEstudiantes.addItem(e.getId() + " - " + e.getNombre());
        }
      }
      selectorPanel.add(cbEstudiantes);

      mainPanel.add(selectorPanel, BorderLayout.NORTH);

      // Email content
      JPanel emailPanel = new JPanel(new BorderLayout(10, 10));
      emailPanel.setBackground(new Color(245, 247, 250));

      // Subject
      JPanel subjectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
      subjectPanel.setBackground(new Color(245, 247, 250));
      subjectPanel.add(new JLabel("Asunto:"));
      JTextField txtAsunto = new JTextField();
      txtAsunto.setPreferredSize(new Dimension(500, 30));
      subjectPanel.add(txtAsunto);

      emailPanel.add(subjectPanel, BorderLayout.NORTH);

      // Body
      JTextArea txtCuerpo = new JTextArea();
      txtCuerpo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
      txtCuerpo.setLineWrap(true);
      txtCuerpo.setWrapStyleWord(true);
      JScrollPane scrollCuerpo = new JScrollPane(txtCuerpo);
      scrollCuerpo.setBorder(BorderFactory.createTitledBorder("Cuerpo del Correo"));

      emailPanel.add(scrollCuerpo, BorderLayout.CENTER);

      mainPanel.add(emailPanel, BorderLayout.CENTER);

      // Buttons
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
      buttonPanel.setBackground(new Color(245, 247, 250));

      JButton btnCargar = crearBoton("Cargar Datos", BTN);
      JButton btnEnviar = crearBoton("Enviar", BTN);
      JButton btnCancelar = crearBoton("Cancelar", BTN2);

      btnCargar.addActionListener(e -> {
        String selected = (String) cbEstudiantes.getSelectedItem();
        if (selected != null) {
          String idStr = selected.split(" - ")[0];
          int id = Integer.parseInt(idStr);
          Estudiante est = estudiantes.stream().filter(estu -> estu.getId() == id).findFirst().orElse(null);
          if (est != null) {
            txtAsunto.setText("Alerta de Riesgo de Deserción Escolar - " + est.getNombre());

            double promC1 = est.getPromedioCorte(1);
            double promC2 = est.getPromedioCorte(2);
            double promC3 = est.getPromedioCorte(3);
            String cuerpo = "Estimado acudiente (" + est.getCorreoAcudiente() + "):\n\n" +
                "Le informamos que el estudiante " + est.getNombre() + " de grado " + est.getGrado() +
                " se encuentra actualmente en riesgo de deserción.\n\n" +
                "Esta situación se ha detectado tras el último seguimiento académico, debido a que el alumno está presentando inasistencias constantes y mantiene un promedio de notas bajas en los cortes registrados hasta la fecha.\n\n"
                +
                "Reporte de apoyo:\n\n" +
                "Número de inasistencias acumuladas: " + est.getTotalFaltas() + "\n\n" +
                "Calificaciones por cortes registrados:\n\n" +
                "Corte 1: " + String.format("%.2f", promC1) + "\n" +
                "Corte 2: " + String.format("%.2f", promC2) + "\n" +
                "Corte 3: " + String.format("%.2f", promC3) + "\n\n" +
                "Le solicitamos encarecidamente comunicarse con la institución o asistir a una cita con el cuerpo docente para revisar este caso y tomar las medidas necesarias para evitar la pérdida del cupo escolar. Alguna duda acérquese a nuestras instalaciones.\n\n"
                +
                "Atentamente,\n\n" +
                "Orientación psicosocial y directivos";
            txtCuerpo.setText(cuerpo);
          }
        }
      });

      btnEnviar.addActionListener(e -> {
        dialog.dispose();
        JOptionPane.showMessageDialog(this, "Mensaje enviado con éxito", "Éxito", JOptionPane.INFORMATION_MESSAGE);
      });

      btnCancelar.addActionListener(e -> dialog.dispose());

      buttonPanel.add(btnCargar);
      buttonPanel.add(btnEnviar);
      buttonPanel.add(btnCancelar);

      mainPanel.add(buttonPanel, BorderLayout.SOUTH);

      dialog.add(mainPanel);
      dialog.setVisible(true);
    }

    private Integer parseIntField(JTextField f, String msg) {
      try {
        return Integer.parseInt(f.getText().trim());
      } catch (Exception e) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        return null;
      }
    }

    private Double parseDoubleField(JTextField f, String msg) {
      try {
        return Double.parseDouble(f.getText().trim());
      } catch (Exception e) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        return null;
      }
    }

    private void refrescarTablaEnTabla(List<Estudiante> lista, DefaultTableModel modelo, JTable tabla) {
      if (lista == null)
        return;

      List<Estudiante> listaOrdenada = new ArrayList<>(lista);
      listaOrdenada.sort((e1, e2) -> {
        int g1 = obtenerPesoGrado(e1.getGrado());
        int g2 = obtenerPesoGrado(e2.getGrado());
        if (g1 != g2)
          return Integer.compare(g1, g2);

        int c = Integer.compare(e1.getCurso(), e2.getCurso());
        if (c != 0)
          return c;

        String n1 = e1.getNombre() == null ? "" : e1.getNombre();
        String n2 = e2.getNombre() == null ? "" : e2.getNombre();
        return n1.compareToIgnoreCase(n2);
      });

      tabla.setSelectionBackground(new Color(232, 241, 250));
      tabla.setSelectionForeground(Color.BLACK);
      tabla.setShowGrid(true);
      tabla.setGridColor(new Color(230, 230, 230));
      tabla.getTableHeader().setBackground(new Color(240, 240, 240));
      tabla.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));

      List<String> columnas = new ArrayList<>();
      columnas.add("ID");
      columnas.add("Nombre");
      columnas.add("Grado");
      columnas.add("Curso");
      columnas.add("Perdidas C1");
      columnas.add("Perdidas C2");
      columnas.add("Perdidas C3");
      columnas.add("Promedio");
      columnas.add("Faltas");
      columnas.add("Estado");
      columnas.add("Acudiente");
      columnas.add("Telefono");
      columnas.add("Correo");

      modelo.setColumnIdentifiers(columnas.toArray(new String[0]));
      modelo.setRowCount(0);
      for (Estudiante e : listaOrdenada) {
        Object[] row = new Object[columnas.size()];
        int k = 0;
        row[k++] = e.getId();
        row[k++] = e.getNombre();
        row[k++] = e.getGrado();
        row[k++] = e.getCurso();
        row[k++] = e.contarMateriasPerdidasCorte(1);
        row[k++] = e.contarMateriasPerdidasCorte(2);
        row[k++] = e.contarMateriasPerdidasCorte(3);
        row[k++] = String.format("%.2f", e.getPromedioGeneral());
        row[k++] = e.getTotalFaltas();
        row[k++] = e.getNivelRiesgo();
        row[k++] = e.getNombreAcudiente();
        row[k++] = e.getTelefonoAcudiente();
        row[k++] = e.getCorreoAcudiente();
        modelo.addRow(row);
      }
    }

    private void mostrarPanelEnDialogo(JPanel panel, String titulo) {
      JDialog dialog = new JDialog(this, titulo, true);
      dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

      JScrollPane scrollPane = new JScrollPane(panel);
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      scrollPane.setBorder(null);

      dialog.getContentPane().add(scrollPane);
      dialog.setSize(650, 700);
      dialog.setLocationRelativeTo(this);
      dialog.setResizable(true);
      dialog.setVisible(true);
    }

  }

  public static Optional<Estudiante> buscarPorId(int id) {
    return estudiantes.stream().filter(e -> e.getId() == id).findFirst();
  }

  private static List<Estudiante> estudiantesAsignadosProfesor(Profesor p) {
    if (p == null) {
      return List.of();
    }
    Set<CursoAcademico> cursos = asignaciones.stream()
        .filter(a -> a != null && p.usuario.equals(a.usuarioProfesor))
        .map(Asignacion::cursoKey)
        .collect(Collectors.toSet());
    if (cursos.isEmpty()) {
      return List.of();
    }
    return estudiantes.stream()
        .filter(e -> cursos.contains(new CursoAcademico(e.getGrado(), e.getCurso())))
        .toList();
  }

  private static void inicializarSistema() {
    instituciones.add(new Institucion("I.E. Técnica Industrial", "admin1", "1234"));
    instituciones.add(new Institucion("Colegio San José", "sanjose", "admin2024"));
    instituciones.add(new Institucion("Liceo Moderno", "liceo", "pass789"));
  }

  private static void guardarDatos() {
    try (BufferedWriter bw = new BufferedWriter(
        new OutputStreamWriter(new FileOutputStream(institucionActual.archivo),
            java.nio.charset.StandardCharsets.UTF_8))) {
      for (Estudiante e : estudiantes) {
        bw.write(e.toDataString());
        bw.newLine();
      }
    } catch (IOException e) {
      System.out.println("Error al guardar.");
    }
  }

  private static void cargarDatos() {
    File f = new File(institucionActual.archivo);
    if (!f.exists())
      return;
    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(new FileInputStream(f), java.nio.charset.StandardCharsets.UTF_8))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] p = line.split("\\|");
        if (p.length < 10)
          continue;

        Map<Integer, List<Double>> notasMap = new HashMap<>();
        if (!p[5].equals("NONE")) {
          String[] cortes = p[5].split(";");
          for (String c : cortes) {
            String[] parts = c.split(":");
            int corteId = Integer.parseInt(parts[0]);
            List<Double> nts = Arrays.stream(parts[1].split(","))
                .map(Double::parseDouble).collect(Collectors.toList());
            notasMap.put(corteId, nts);
          }
        }
        Map<String, Map<Integer, List<Double>>> notasMateria = new HashMap<>();
        if (p.length >= 11 && !p[10].equals("NONE")) {
          notasMateria = parseNotasPorMateria(p[10]);
        }
        Map<Integer, Integer> faltasMes = new HashMap<>();
        if (p.length >= 12 && !p[11].equals("NONE")) {
          faltasMes = parseFaltasPorMes(p[11]);
        }
        int mesRegistro = p.length >= 13 ? Integer.parseInt(p[12]) : java.time.LocalDate.now().getMonthValue();
        estudiantes.add(new Estudiante(Integer.parseInt(p[0]), p[1], Integer.parseInt(p[2]),
            p[3], Integer.parseInt(p[4]), notasMap, notasMateria, faltasMes, Integer.parseInt(p[6]), p[7], p[8], p[9],
            mesRegistro));
      }
    } catch (Exception e) {
      System.out.println("Aviso: Formato de datos actualizado.");
    }
  }

  private static String archivoProfesores(Institucion inst) {
    return "profesores_" + inst.usuario + ".txt";
  }

  private static String archivoAsignaciones(Institucion inst) {
    return "asignaciones_" + inst.usuario + ".txt";
  }

  private static void cargarProfesoresYAsignaciones() {
    profesores.clear();
    asignaciones.clear();
    if (institucionActual == null) {
      return;
    }
    cargarProfesores(institucionActual, profesores);
    cargarAsignaciones(institucionActual, asignaciones);
  }

  private static void cargarProfesores(Institucion inst, List<Profesor> out) {
    File f = new File(archivoProfesores(inst));
    if (!f.exists())
      return;
    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(new FileInputStream(f), java.nio.charset.StandardCharsets.UTF_8))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] p = line.split("\\|");
        if (p.length < 3)
          continue;
        out.add(new Profesor(p[0].trim(), p[1].trim(), p[2].trim()));
      }
    } catch (Exception e) {
      System.out.println("Aviso: No se pudieron cargar profesores.");
    }
  }

  private static void guardarProfesores() {
    if (institucionActual == null)
      return;
    try (BufferedWriter bw = new BufferedWriter(
        new OutputStreamWriter(new FileOutputStream(archivoProfesores(institucionActual)),
            java.nio.charset.StandardCharsets.UTF_8))) {
      for (Profesor p : profesores) {
        bw.write(p.usuario + "|" + p.password + "|" + p.nombre);
        bw.newLine();
      }
    } catch (IOException e) {
      System.out.println("Error al guardar profesores.");
    }
  }

  private static void cargarAsignaciones(Institucion inst, List<Asignacion> out) {
    File f = new File(archivoAsignaciones(inst));
    if (!f.exists())
      return;
    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(new FileInputStream(f), java.nio.charset.StandardCharsets.UTF_8))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] p = line.split("\\|");
        if (p.length < 4)
          continue;
        try {
          out.add(new Asignacion(p[0].trim(), Integer.parseInt(p[1].trim()), p[2].trim(), p[3].trim()));
        } catch (Exception ex) {
        }
      }
    } catch (Exception e) {
      System.out.println("Aviso: No se pudieron cargar asignaciones.");
    }
  }

  private static void guardarAsignaciones() {
    if (institucionActual == null)
      return;
    try (BufferedWriter bw = new BufferedWriter(
        new OutputStreamWriter(new FileOutputStream(archivoAsignaciones(institucionActual)),
            java.nio.charset.StandardCharsets.UTF_8))) {
      for (Asignacion a : asignaciones) {
        bw.write((a.grado == null ? "" : a.grado.trim()) + "|" + a.curso + "|" + a.materia + "|" + a.usuarioProfesor);
        bw.newLine();
      }
    } catch (IOException e) {
      System.out.println("Error al guardar asignaciones.");
    }
  }

  private static Map<String, Map<Integer, List<Double>>> parseNotasPorMateria(String field) {
    Map<String, Map<Integer, List<Double>>> out = new HashMap<>();
    if (field == null)
      return out;
    String s = field.trim();
    if (s.isEmpty() || "NONE".equalsIgnoreCase(s))
      return out;
    String[] materias = s.split("§");
    for (String m : materias) {
      if (m == null || m.trim().isEmpty())
        continue;
      String[] mp = m.split("@", 2);
      if (mp.length < 2)
        continue;
      String nombre = mp[0].trim();
      String cortesStr = mp[1].trim();
      if (nombre.isEmpty() || cortesStr.isEmpty())
        continue;
      Map<Integer, List<Double>> cortes = new HashMap<>();
      String[] cortesItems = cortesStr.split(";");
      for (String c : cortesItems) {
        if (c == null || c.trim().isEmpty())
          continue;
        String[] parts = c.split(":", 2);
        if (parts.length < 2)
          continue;
        try {
          int corteId = Integer.parseInt(parts[0].trim());
          List<Double> nts = Arrays.stream(parts[1].split(","))
              .filter(x -> x != null && !x.trim().isEmpty())
              .map(x -> {
                try {
                  return Double.parseDouble(x.trim());
                } catch (Exception ex) {
                  return null;
                }
              })
              .filter(Objects::nonNull)
              .collect(Collectors.toList());
          cortes.put(corteId, nts);
        } catch (Exception ex) {
        }
      }
      out.put(nombre, cortes);
    }
    return out;
  }

  private static Map<Integer, Integer> parseFaltasPorMes(String field) {
    Map<Integer, Integer> out = new HashMap<>();
    if (field == null)
      return out;
    String s = field.trim();
    if (s.isEmpty() || "NONE".equalsIgnoreCase(s))
      return out;
    String[] items = s.split(";");
    for (String it : items) {
      if (it == null || it.trim().isEmpty())
        continue;
      String[] p = it.split("=", 2);
      if (p.length < 2)
        continue;
      try {
        int mes = Integer.parseInt(p[0].trim());
        int faltas = Integer.parseInt(p[1].trim());
        if (mes < 1 || mes > 12)
          continue;
        if (faltas < 0)
          continue;
        out.put(mes, faltas);
      } catch (Exception ex) {
      }
    }
    return out;
  }

  private static void exportarGenericoCSV(List<Estudiante> filtrados, String nom) {
    int maxNotas = filtrados.stream()
        .mapToInt(e -> e.getNotasPorCorte().values().stream().mapToInt(List::size).sum())
        .max().orElse(0);

    try (FileWriter fw = new FileWriter(nom)) {
      StringBuilder header = new StringBuilder("ID;Nombre;Grado;Curso");
      for (int i = 1; i <= maxNotas; i++) {
        header.append(";Nota").append(i);
      }
      header.append(";Promedio;Faltas;Estado;NombreAcudiente;CorreoAcudiente;TelefonoAcudiente\n");
      fw.write(header.toString());

      for (Estudiante e : filtrados) {
        StringBuilder row = new StringBuilder();
        row.append(e.getId()).append(";")
            .append(e.getNombre()).append(";")
            .append(e.getGrado()).append(";")
            .append(e.getCurso());

        List<Double> todasLasNotas = e.getNotasPorCorte().keySet().stream()
            .sorted()
            .flatMap(c -> e.getNotasPorCorte().get(c).stream())
            .collect(Collectors.toList());

        for (int i = 0; i < maxNotas; i++) {
          if (i < todasLasNotas.size()) {
            row.append(";").append(String.format("%.2f", todasLasNotas.get(i)));
          } else {
            row.append(";0.00");
          }
        }

        row.append(";").append(String.format("%.2f", e.getPromedioGeneral()))
            .append(";").append(e.getTotalFaltas())
            .append(";").append(e.getNivelRiesgo())
            .append(";").append(e.getNombreAcudiente())
            .append(";").append(e.getCorreoAcudiente())
            .append(";").append(e.getTelefonoAcudiente())
            .append("\n");

        fw.write(row.toString());
      }
      System.out.println("Reporte profesional generado: " + nom);
    } catch (IOException e) {
      System.out.println("Error al exportar: " + e.getMessage());
    }
  }
}

class PanelBarrasRiesgo extends JPanel {
  private final List<Estudiante> estudiantes;

  public PanelBarrasRiesgo(List<Estudiante> estudiantes) {
    this.estudiantes = estudiantes;
    setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(new Color(41, 128, 185), 2),
        "Cantidad de Estudiantes por Estado",
        javax.swing.border.TitledBorder.CENTER,
        javax.swing.border.TitledBorder.TOP,
        new Font("Segoe UI", Font.BOLD, 14),
        new Color(41, 128, 185)));
    setBackground(new Color(245, 247, 250));
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    long normal = estudiantes.stream().filter(e -> e.getNivelRiesgo().equalsIgnoreCase("NORMAL")).count();
    long alertaD = estudiantes.stream().filter(e -> e.getNivelRiesgo().equalsIgnoreCase("ALERTA DE DESERCIÓN")).count();
    long riesgoD = estudiantes.stream().filter(e -> e.getNivelRiesgo().equalsIgnoreCase("RIESGO DE DESERCIÓN")).count();
    long alertaA = estudiantes.stream().filter(e -> e.getNivelRiesgo().equalsIgnoreCase("ALERTA ACADÉMICA")).count();
    long alertaI = estudiantes.stream().filter(e -> e.getNivelRiesgo().equalsIgnoreCase("ALERTA DE ASISTENCIA"))
        .count();

    long max = Math.max(normal, Math.max(alertaD, Math.max(riesgoD, Math.max(alertaA, alertaI))));
    if (max == 0)
      max = 1;

    int width = getWidth() - 100;
    int height = getHeight() - 120;
    int barWidth = width / 5;

    dibujarBarra(g2d, 50, (int) normal, (int) max, barWidth, height, "NORMAL", new Color(46, 204, 113));
    dibujarBarra(g2d, 50 + barWidth, (int) alertaD, (int) max, barWidth, height, "ALERTA DES", new Color(241, 196, 15));
    dibujarBarra(g2d, 50 + barWidth * 2, (int) riesgoD, (int) max, barWidth, height, "RIESGO DES",
        new Color(231, 76, 60));
    dibujarBarra(g2d, 50 + barWidth * 3, (int) alertaA, (int) max, barWidth, height, "ALERTA ACAD",
        new Color(52, 152, 219));
    dibujarBarra(g2d, 50 + barWidth * 4, (int) alertaI, (int) max, barWidth, height, "ALERTA ASIS",
        new Color(155, 89, 182));
  }

  private void dibujarBarra(Graphics2D g, int x, int valor, int max, int w, int h, String label, Color c) {
    int barHeight = (int) ((double) valor / max * (h - 40));

    // Gradiente para la barra
    GradientPaint gradient = new GradientPaint(
        x + 10, h - barHeight + 40, c,
        x + 10, h + 40, c.darker());
    g.setPaint(gradient);
    g.fillRoundRect(x + 10, h - barHeight + 40, w - 20, barHeight, 10, 10);

    // Borde
    g.setColor(c.darker().darker());
    g.drawRoundRect(x + 10, h - barHeight + 40, w - 20, barHeight, 10, 10);

    // Valor
    g.setColor(Color.BLACK);
    g.setFont(new Font("Segoe UI", Font.BOLD, 12));
    String valorStr = String.valueOf(valor);
    int valorWidth = g.getFontMetrics().stringWidth(valorStr);
    g.drawString(valorStr, x + w / 2 - valorWidth / 2, h - barHeight + 30);

    // Etiqueta
    g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    int labelWidth = g.getFontMetrics().stringWidth(label);
    g.drawString(label, x + w / 2 - labelWidth / 2, h + 70);
  }
}

class PanelBarrasGrados extends JPanel {
  private final List<Estudiante> estudiantes;

  public PanelBarrasGrados(List<Estudiante> estudiantes) {
    this.estudiantes = estudiantes;
    setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(new Color(41, 128, 185), 2),
        "Estudiantes por Grado",
        javax.swing.border.TitledBorder.CENTER,
        javax.swing.border.TitledBorder.TOP,
        new Font("Segoe UI", Font.BOLD, 14),
        new Color(41, 128, 185)));
    setBackground(new Color(245, 247, 250));
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    Map<String, Long> porGrado = estudiantes.stream()
        .collect(Collectors.groupingBy(Estudiante::getGrado, Collectors.counting()));

    int x = 50;
    int barWidth = (getWidth() - 100) / Math.max(1, porGrado.size());
    long max = porGrado.values().stream().max(Long::compare).orElse(1L);
    int h = getHeight() - 120;

    int i = 0;
    for (Map.Entry<String, Long> entry : porGrado.entrySet()) {
      int val = entry.getValue().intValue();
      int barHeight = (int) ((double) val / max * (h - 40));

      // Gradiente para la barra
      Color baseColor = new Color(100, 150, 255);
      GradientPaint gradient = new GradientPaint(
          x + i * barWidth + 10, h - barHeight + 40, baseColor,
          x + i * barWidth + 10, h + 40, baseColor.darker());
      g2d.setPaint(gradient);
      g2d.fillRoundRect(x + i * barWidth + 10, h - barHeight + 40, barWidth - 20, barHeight, 10, 10);

      // Borde
      g2d.setColor(baseColor.darker().darker());
      g2d.drawRoundRect(x + i * barWidth + 10, h - barHeight + 40, barWidth - 20, barHeight, 10, 10);

      // Valor
      g2d.setColor(Color.BLACK);
      g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
      String valorStr = String.valueOf(val);
      int valorWidth = g2d.getFontMetrics().stringWidth(valorStr);
      g2d.drawString(valorStr, x + i * barWidth + barWidth / 2 - valorWidth / 2, h - barHeight + 30);

      // Etiqueta
      g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
      String label = entry.getKey();
      int labelWidth = g2d.getFontMetrics().stringWidth(label);
      g2d.drawString(label, x + i * barWidth + barWidth / 2 - labelWidth / 2, h + 70);

      i++;
    }
  }
}