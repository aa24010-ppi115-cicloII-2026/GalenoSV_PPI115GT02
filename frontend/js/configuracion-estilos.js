import { configurarEstilos } from "./base.js";

configurarEstilos({
  base: new URL("../css/componentes/base.css", import.meta.url).href,
  directorio: new URL("../css/componentes/", import.meta.url).href,
});
