let createFormClassName = "create-form";
let deleteFormClassName = "delete-form";
let updateFormClassName = "update-form";
let readFormClassName = "read-form";

function componentRoute(action) {
  if (action === "delete") {
    createFormClassName = "create-form";
    deleteFormClassName = "delete-form-active";
    updateFormClassName = "update-form";
    readFormClassName = "read-form";
  } else if (action === "create") {
    createFormClassName = "create-form-active";
    deleteFormClassName = "delete-form";
    updateFormClassName = "update-form";
    readFormClassName = "read-form";
  } else if (action === "update") {
    createFormClassName = "create-form";
    deleteFormClassName = "delete-form";
    updateFormClassName = "update-form-active";
    readFormClassName = "read-form";
  } else if (action === "read") {
    createFormClassName = "create-form";
    deleteFormClassName = "delete-form";
    updateFormClassName = "update-form";
    readFormClassName = "read-form-active";
  } else {
    createFormClassName = "create-form";
    deleteFormClassName = "delete-form";
    updateFormClassName = "update-form";
    readFormClassName = "read-form";
  }
  document.querySelector("#create").className = createFormClassName;
  document.querySelector("#delete").className = deleteFormClassName;
  document.querySelector("#update").className = updateFormClassName;
  document.querySelector("#read").className = readFormClassName;
}
