// exports Regular expressions used for Validators in patterns to check if the value is valid

const mailRegex = new RegExp(
  /^(([^<>()\[\]\\.,;:\s@']+(\.[^<>()\[\]\\.,;:\s@']+)*)|('.+'))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/,
);
const passwordRegex = new RegExp(/^(?=.*[a-z])(?=.*[A-Z])(?=.*[.@!?_-])(?=.*\d)[A-Za-z\d.@!?_-]{8,}$/);

const alphabeticRegex = new RegExp(/^[A-Za-zÁÉÍÓÚñáéíóúÑüëÜË\s.]+$/);
const codeLanguagesRegex = new RegExp(
  /^[A-Za-zÁÉÍÓÚñáéíóúÑüëÜË0-9.,'&#+_-\s]*[A-Za-z][A-Za-zÁÉÍÓÚñáéíóúÑüëÜË0-9.,'&#+_-\s]{0,}$/g,
);
const noEmojisRegex = new RegExp(
  /^\S(?!.*(\u00a9|\u00ae|[\u2000-\u3300]|\ud83c[\ud000-\udfff]|\ud83d[\ud000-\udfff]|\ud83e[\ud000-\udfff]))[A-Za-zÁÉÍÓÚñáéíóúÑñüëÜË0-9.,'"&_#$%*^()*[\]?\\!/|<>;:@-\s]+$/,
);
const alphaNumberRegex = new RegExp(
  /^[A-Za-zÁÉÍÓÚñáéíóúÑüëÜË0-9.,'&-_#\s]*[A-Za-zÁÉÍÓÚñáéíóúÑüëÜË][A-Za-zÁÉÍÓÚñáéíóúÑüëÜË0-9.,'&-_#+\s]{0,}$/g,
);

const alphabeticNoEmojisRegex = new RegExp(/^[A-Za-zÁÉÍÓÚñáéíóúÑñüëÜË.,'"&_#$%*^()*[\]?\\!/|<>;:@+-\s]+$/);
const alphaNumberNoEmojisRegex = new RegExp(
  /^[A-Za-zÁÉÍÓÚñáéíóúÑñüëÜË0-9.,'"&_#=$%*^()*[\]?\\!/|<>;:@+-\s]*[a-zA-Z0-9][A-Za-zÁÉÍÓÚñáéíóúÑñüëÜË0-9.,'"&_#=$%*^()*[\]?\\!/|<>;:@+-\s]*$/,
);
const mailRegexNoEmojis = new RegExp(/^[a-zA-Z0-9!#$%&'*+-/=?^_`{|}~.]+@[a-zA-Z0-9-]+[.][a-zA-Z0-9-.]+$/);
const rolesRegex = new RegExp(/^[A-Za-zÁÉÍÓÚñáéíóúÑüëÜË0-9. -]*[A-Za-z][A-Za-zÁÉÍÓÚñáéíóúÑüëÜË0-9. -]{0,}$/g);
const projectRoleRegex = new RegExp(/^[A-Za-zÁÉÍÓÚñáéíóúÑüëÜË0-9. -]*[A-Za-z][A-Za-zÁÉÍÓÚñáéíóúÑüëÜË0-9. -]{0,}$/g);
const regionNameRegex = new RegExp(/^(\ *[A-Za-zÁÉÍÓÚñáéíóúÑüëÜË]{1,}\.{0,1}\-{0,1}\ {0,1}){1,}\ *$/);
const locationNameRegex = new RegExp(/^(\ *[A-Za-zÁÉÍÓÚñáéíóúÑüëÜË]{1,}\.{0,1}\ {0,1}){1,}\ *$/);
const fieldRegex = new RegExp(/^[A-Za-zÁÉÍÓÚáéíóúñÑ0-9.#\s+-]*[A-Za-z][A-Za-zÁÉÍÓÚñáéíóúÑüëÜË0-9.#+-\s]{0,}$/);
const seniorityRegex = new RegExp(/^[A-Za-zÁÉÍÓÚñáéíóúÑüëÜË0-9. -]*[A-Za-z][A-Za-zÁÉÍÓÚñáéíóúÑüëÜË0-9. -]{0,}$/);

export {
  mailRegex,
  passwordRegex,
  alphabeticRegex,
  codeLanguagesRegex,
  noEmojisRegex,
  alphaNumberRegex,
  alphabeticNoEmojisRegex,
  alphaNumberNoEmojisRegex,
  mailRegexNoEmojis,
  rolesRegex,
  regionNameRegex,
  locationNameRegex,
  fieldRegex,
  seniorityRegex,
  projectRoleRegex,
};
