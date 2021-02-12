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

  $("#blockly-modal").on("hide.bs.modal", function (e) {
    // TODO(Richo): Validate entire form and cancel close, if invalid!
    close();
    close = nop;
  });

  function getFormData() {
    let data = $("#blockly-modal-container").serializeJSON();
    if (data.elements == undefined) return [];
    return Object.keys(data.elements).map(k => data.elements[k]);
  }

  function build(spec) {
    $("#blockly-modal-container-thead").html("");
    $("#blockly-modal-container-tbody").html("");
    $("#blockly-modal-title").text(spec.title);
    buildHead(spec.columns);
    buildBody(spec);
  }

  function buildHead(columns) {
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

  function buildBody(spec) {
    let columns = spec.columns;
    let rows = spec.rows;
    let validateForm = spec.validateForm || nop;
    let buildRow = (row, i) => {
      let tr = $("<tr>");
      tr.append($("<input>").attr("type", "hidden").attr("name", "elements[" + i + "][index]").attr("value", i));
      columns.forEach(col => {
        let buildInput = inputs[col.type] || inputs["text"];
        let value = row[col.id];
        tr.append($("<td>").append(buildInput(value, i, col.id)));
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

  let inputs = {
    "text": (value, i, id, validationFn) => {
      let input = $("<input>")
        .attr("type", "text")
        .addClass("form-control")
        .addClass("text-center")
        .css("padding-right", "initial") // Fix for weird css alignment issue when is-invalid
        .attr("name", "elements[" + i + "][" + id + "]");
      if (validationFn != undefined) {
        input.on("keyup", validationFn);
      }
      input.get(0).value = value;
      return input;
    }
  }

  function show(spec) {
    build(spec);
    return new Promise(res => {
      $("#blockly-modal").modal("show");
      close = () => res(getFormData());
    });
  }

  return {
    show: show
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
      {name: "Nombre del elemento", id: "elementName", type: "text"},
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
