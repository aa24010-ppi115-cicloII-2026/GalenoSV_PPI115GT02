export const CODIGOS_CIE10 = [
  { codigo: "K00.0", descripcion: "Anodoncia" },
  { codigo: "K00.6", descripcion: "Alteraciones de la erupción dentaria" },
  { codigo: "K01.1", descripcion: "Diente incluido" },
  { codigo: "K02.0", descripcion: "Caries limitada al esmalte" },
  { codigo: "K02.1", descripcion: "Caries de la dentina" },
  { codigo: "K02.2", descripcion: "Caries del cemento" },
  { codigo: "K02.9", descripcion: "Caries dental no especificada" },
  { codigo: "K03.0", descripcion: "Atrición excesiva de los dientes" },
  { codigo: "K03.1", descripcion: "Abrasión de los dientes" },
  { codigo: "K03.2", descripcion: "Erosión de los dientes" },
  { codigo: "K03.6", descripcion: "Depósitos en los dientes" },
  { codigo: "K04.0", descripcion: "Pulpitis" },
  { codigo: "K04.1", descripcion: "Necrosis de la pulpa" },
  { codigo: "K04.4", descripcion: "Periodontitis apical aguda" },
  { codigo: "K04.5", descripcion: "Periodontitis apical crónica" },
  { codigo: "K04.7", descripcion: "Absceso periapical sin fístula" },
  { codigo: "K05.0", descripcion: "Gingivitis aguda" },
  { codigo: "K05.1", descripcion: "Gingivitis crónica" },
  { codigo: "K05.3", descripcion: "Periodontitis crónica" },
  { codigo: "K06.0", descripcion: "Retracción gingival" },
  { codigo: "K07.0", descripcion: "Anomalías del tamaño de los maxilares" },
  { codigo: "K07.3", descripcion: "Anomalías de la posición de los dientes" },
  { codigo: "K08.1", descripcion: "Pérdida de dientes por accidente o extracción" },
  { codigo: "K08.3", descripcion: "Raíz dental retenida" },
  { codigo: "K09.0", descripcion: "Quiste odontogénico de desarrollo" },
  { codigo: "K10.2", descripcion: "Inflamación de los maxilares" },
  { codigo: "K11.2", descripcion: "Sialoadenitis" },
  { codigo: "K12.0", descripcion: "Aftas recurrentes" },
  { codigo: "K12.1", descripcion: "Estomatitis" },
  { codigo: "K13.0", descripcion: "Enfermedades de los labios" },
  { codigo: "K14.0", descripcion: "Glositis" },
  { codigo: "K14.6", descripcion: "Glosodinia" },
  { codigo: "R51", descripcion: "Cefalea" },
  { codigo: "R68.8", descripcion: "Otros síntomas generales especificados" },
  { codigo: "Z01.2", descripcion: "Examen odontológico" },
  { codigo: "Z29.0", descripcion: "Aislamiento profiláctico" },
  { codigo: "Z76.0", descripcion: "Emisión de receta repetida" },
];

export function buscarCie10(texto, limite = 8) {
  const consulta = String(texto ?? "").trim().toLowerCase();
  if (!consulta) return [];
  return CODIGOS_CIE10.filter(
    ({ codigo, descripcion }) =>
      codigo.toLowerCase().includes(consulta) || descripcion.toLowerCase().includes(consulta)
  ).slice(0, limite);
}

export function cie10PorCodigo(codigo) {
  return CODIGOS_CIE10.find((item) => item.codigo === codigo) ?? null;
}
