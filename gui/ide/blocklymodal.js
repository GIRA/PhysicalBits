let BlocklyModal = (function () {
  /*
  {
    title: "",
    columns: [{name: "", id: "", type: ""}, ...],
    rows: [{index: 0, removable: t/f, data: {..}}]
  }
  */

  let nop = () => {};
  let close = nop;
  let spec = null;

  $("#blockly-modal").on("hide.bs.modal", function (evt) {
    if (!validateForm()) {
      evt.preventDefault();
      evt.stopImmediatePropagation();
      return;
    }

    close();
    close = nop;
  });

  $("#blockly-modal-container").on("submit", function (e) {
    e.preventDefault();
    $("#blockly-modal").modal("hide");
  });

  function getFormData() {
    let data = $("#blockly-modal-container").serializeJSON();
    if (data.elements == undefined) return [];
    return Object.keys(data.elements).map(k => data.elements[k]);
  }

  function getInputs() {
    let inputs = $("#blockly-modal-container input").filter(function () {
      return this.type !== "hidden";
    });

    let results = {};
    let regex = /\w+\[(\d+)\]\[(\w+)\]/;
    for (let i = 0; i < inputs.length; i++) {
      let input = inputs[i];
      let name_groups = regex.exec(input.name);
      let index = name_groups[1];
      let name = name_groups[2];
      if (results[index] == undefined) {
        results[index] = {};
      }
      results[index][name] = input;
    }
    return results;
  }

  function validateForm() {
    let data = getFormData();
    let inputs = getInputs();
    let result = true;
    for (let i = 0; i < data.length; i++) {
      let row = data[i];
      let input_group = inputs[row.index];
      if (!input_group) continue;

      let keys = Object.keys(row);
      for (let j = 0; j < keys.length; j++) {
        let key = keys[j];
        let input = input_group[key];
        let col = spec.columns.find(c => c.id == key);
        if (!col) continue;

        let validations = validationsByType[col.type] || [];
        if (validations.length > 0 && input) {
          let valid = validateInput(input, key, data, validations);
          result = result && valid;
        }
      }
    }
    return result;
  }

  function validateInput(input, id, data, validations) {
    let valid = validations.every(v => v(id, input.value, data));
    if (valid) {
      input.classList.remove("is-invalid");
    } else {
      input.classList.add("is-invalid");
    }
    return valid;
  }

  function build() {
    $("#blockly-modal-container-thead").html("");
    $("#blockly-modal-container-tbody").html("");
    $("#blockly-modal-title").text(spec.title);
    buildHead();
    buildBody();
  }

  function buildHead() {
    let columns = spec.columns;
    let tr = $("<tr>");
    for (let i = 0; i < columns.length; i++) {
      tr.append($("<th>")
        .addClass("text-center")
        .text(columns[i].name))
    }
    tr.append($("<th>")
      .addClass("text-center")
      .append($("<button>")
        .attr("id", "blockly-modal-add-row-btn")
        .attr("type", "button")
        .addClass("btn btn-sm btn-outline-success")
        .append($("<i>").addClass("fas fa-plus"))));
    $("#blockly-modal-container-thead").append(tr);
  }

  function buildBody() {
    let columns = spec.columns;
    let rows = spec.rows;
    let buildRow = (row, i) => {
      let tr = $("<tr>");
      tr.append($("<input>").attr("type", "hidden").attr("name", "elements[" + i + "][index]").attr("value", i));
      columns.forEach(col => {
        let buildInput = inputs[col.type] || inputs["text"];
        let value = row[col.id];
        let validations = validationsByType[col.type] || [];
        tr.append($("<td>").append(buildInput(value, i, col.id, validations)));
      });

      let btn = $("<button>")
        .addClass("btn")
        .addClass("btn-sm")
        .attr("type", "button")
        .append($("<i>")
          .addClass("fas")
          .addClass("fa-minus"));
      if (row.removable) {
        btn
          .addClass("btn-outline-danger")
          .on("click", function () { tr.remove(); validateForm(); });
      } else {
        btn.addClass("btn-outline-secondary");
        if (spec.cantRemoveMsg) {
          btn
            .attr("data-toggle", "tooltip")
            .attr("data-placement", "left")
            .attr("title", spec.cantRemoveMsg)
            .on("click", function () { btn.tooltip("toggle"); });
        }
      }
      tr.append($("<td>").append(btn));

      $("#blockly-modal-container-tbody").append(tr);
    };

    rows.forEach(buildRow);

    $("#blockly-modal-add-row-btn").on("click", function () {
      let data = getFormData();
      let nextIndex = data.length == 0 ? 0: 1 + Math.max.apply(null, data.map(m => m.index));
      let element = spec.defaultElement(data);
      if (element.removable !== false) {
        element.removable = true;
      }
      buildRow(element, nextIndex);
    });
  }

  let inputs = (function () {
    let text = (value, i, id, validations) => {
      let input = $("<input>")
        .attr("type", "text")
        .addClass("form-control")
        .addClass("text-center")
        .css("padding-right", "initial") // Fix for weird css alignment issue when is-invalid
        .attr("name", "elements[" + i + "][" + id + "]");
      input.on("keyup", function () {
        if (validations.length == 0) return false;
        let data = getFormData();
        return validateInput(this, id, data, validations);
      });
      input.get(0).value = value;
      return input;
    };
    let pin = (value, i, id, validations) => {
      let select = $("<select>")
        .addClass("form-control")
        .attr("name", "elements[" + i + "][" + id + "]");
      Uzi.state.pins.available.forEach(function (pin) {
        select.append($("<option>").text(pin.name));
      });
      select.get(0).value = value;
      return select;
    };

    return {
      text: text,
      pin: pin,
      identifier: text,
      number: text
    };
  })();

  let validationsByType = (function () {
    let identifier = (id, value, data) => {
      let regex = /^[a-zA-Z_][a-zA-Z_0-9]*$/;
      return regex.test(value);
    };
    let unique = (id, value, data) => {
      let other = data.map(each => each[id]);
      return other.filter(each => each == value).length == 1;
    };
    let number = (id, value, data) => {
      if (value == "Infinity") return true;
      if (value == "-Infinity") return true;
      if (value == "NaN") return true;
      let regex = /^[0-9]+(\.[0-9]+)?$/;
      return regex.test(value);
    };

    return {
      identifier: [identifier, unique],
      number: [number],
    }
  })();

  function show(s) {
    spec = s;
    build();
    return new Promise(res => {
      close = () => res(getFormData());
      $("#blockly-modal").modal("show");
      validateForm();
    });
  }

  return {
    show: show,

    // TODO(Richo): Testing
    getFormData: getFormData,
    getInputs: getInputs,
  };

})();

function test_modals() {
  let spec = {
    title: "Testing Blockly Modals...",
    cantRemoveMsg: "This element is being used by the program!",
    defaultElement: (data) => {
      let names = new Set(data.map(m => m.elementName));
      let element = { elementName: "juan", elementPin: "D10", elementNum: "1" };
      let i = 1;
      while (names.has(element.elementName)) {
        element.elementName = "juan" + i;
        i++;
      }
      return element;
    },
    columns: [
      {name: "Nombre del elemento", id: "elementName", type: "identifier"},
      {name: "Pin del elemento", id: "elementPin", type: "pin"},
      {name: "Num del elemento", id: "elementNum", type: "number"}
    ],
    rows: [
      {
        elementName: "Richo",
        elementPin: "D8",
        elementNum: 42,
        removable: true
      },
      {
        elementName: "Diego",
        elementPin: "D13",
        elementNum: 28,
      },
      {
        elementName: "Sofía",
        elementPin: "A5",
        elementNum: 23,
        removable: false
      }
    ]
  }
  BlocklyModal.show(spec).then(data => console.log(data));
}
