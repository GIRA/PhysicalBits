
/**
 * Generate code for the specified block (and attached blocks).
 * @param {Blockly.Block} block The block to generate code for.
 * @return {string|!Array} For statement blocks, the generated code.
 *     For value blocks, an array containing the generated code and an
 *     operator order value.  Returns '' if block is null.
 */
Blockly.JavaScript.blockToCode = function(block) {
  if (!block) {
    return '';
  }
  if (block.disabled) {
    // Skip past this block if it is disabled.
    return this.blockToCode(block.getNextBlock());
  }

  var func = this[block.type];
  goog.asserts.assertFunction(func,
      'Language "%s" does not know how to generate code for block type "%s".',
      this.name_, block.type);
  // First argument to func.call is the value of 'this' in the generator.
  // Prior to 24 September 2013 'this' was the only way to access the block.
  // The current prefered method of accessing the block is through the second
  // argument to func.call, which becomes the first parameter to the generator.
  var code = func.call(block, block);
  if (goog.isArray(code)) {
    // Value blocks return tuples of code and operator order.
    goog.asserts.assert(block.outputConnection,
        'Expecting string from statement block "%s".', block.type);
    return [this.scrub_(block, code[0]), code[1]];
  } else if (goog.isString(code)) {
    var id = block.id.replace(/\$/g, '$$$$');  // Issue 251.
    if (this.STATEMENT_PREFIX) {
      code = this.STATEMENT_PREFIX.replace(/%1/g, '\'' + id + '\'') +
          code;
    }
    return this.scrub_(block, code);
  } else if (code === null) {
    // Block has handled code generation itself.
    return '';
  } else {
    goog.asserts.fail('Invalid code generated: %s', code);
  }
};

Blockly.JavaScript.scrub_ = function(block, code) {
	var nextBlock = block.nextConnection && block.nextConnection.targetBlock();
	var nextCode = Blockly.JavaScript.blockToCode(nextBlock);
	var json = code === '' ? null : JSON.parse(code);
	var nextJSON = nextCode === '' ? null : JSON.parse(nextCode);
	var result = [];
	if (json !== null) {
		result.push(json);
	}
	if (nextJSON !== null) {
		nextJSON.forEach(function (each) { 
			result.push(each);
		});
	}
	return JSON.stringify(result);
};

Blockly.JavaScript['math_number'] = function(block) {
	// Numeric value.
	var code = NumberNode(parseFloat(block.getFieldValue('NUM')));
	code = JSON.stringify(code);
	return [code, Blockly.JavaScript.ORDER_ATOMIC];
};


Blockly.JavaScript['math_arithmetic'] = function(block) {
	// Basic arithmetic operators, and power.
	var OPERATORS = {
		'ADD': '+',
		'MINUS': '-',
		'MULTIPLY': '*',
		'DIVIDE': '/',
		'POWER': '**'
	};
	var order = Blockly.JavaScript.ORDER_NONE;
	var operator = OPERATORS[block.getFieldValue('OP')];
	var argument0 = Blockly.JavaScript.valueToCode(block, 'A', order) || '0';
	var argument1 = Blockly.JavaScript.valueToCode(block, 'B', order) || '0';
	var code = MessageSendNode({
		receiver: eval(argument0),
		selector: operator,
		arguments: [eval(argument1)]
	});
	code = JSON.stringify(code);
	return [code, order];
};

Blockly.JavaScript['math_single'] = function(block) {
	// Math operators with single operand.
	var operator = block.getFieldValue('OP');
	var rcvr = Blockly.JavaScript.valueToCode(block, 'NUM', Blockly.JavaScript.ORDER_NONE) || '0';	
	var selector;
	var args = [];
	switch (operator) {
		case 'NEG': selector = "negated"; break;
		case 'SIN': selector = "sin"; break;
		case 'COS': selector = "cos"; break;
		case 'TAN': selector = "tan"; break;
		case 'ABS': selector = "abs"; break;
		case 'ROOT': selector = "sqrt"; break;
		case 'LN': selector = "ln"; break;
		case 'EXP': selector = "exp"; break;
		case 'POW10':
			args = [rcvr];
			rcvr = 10;
			selector = '**';
			break;
		case 'ROUND': selector = "round"; break;
		case 'ROUNDUP': selector = "ceiling"; break;
		case 'ROUNDDOWN': selector = "floor"; break;
		case 'LOG10': selector = "log"; break;
		case 'ASIN': selector = "arcSin"; break;
		case 'ACOS': selector = "arcCos"; break;
		case 'ATAN': selector = "arcTan"; break;
		default:
			throw 'Unknown math operator: ' + operator;
	}
	var code = MessageSendNode({
		receiver: rcvr,
		selector: selector,
		arguments: args
	});
	code = JSON.stringify(code);
	return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['math_constant'] = function(block) {
	// Constants: PI, E, the Golden Ratio, sqrt(2), 1/sqrt(2), INFINITY.
	var CONSTANTS = {
		'PI': Math.PI,
		'E': Math.E,
		'GOLDEN_RATIO': (1 + Math.sqrt(5)) / 2,
		'SQRT2': Math.SQRT2,
		'SQRT1_2': Math.SQRT1_2,
		'INFINITY': Infinity
	};
	var val = CONSTANTS[block.getFieldValue('CONSTANT')];
	var code = NumberNode(val);
	code = JSON.stringify(code);
	return [code, Blockly.JavaScript.ORDER_NONE];
};




Blockly.JavaScript['script'] = function(block) {
	var text_scriptname = block.getFieldValue('scriptName');
	var number_tickingtimes = block.getFieldValue('tickingTimes');
	var dropdown_tickingscale = block.getFieldValue('tickingScale');
	var statements_statements = Blockly.JavaScript.statementToCode(block, 'statements');
	
	var scale;
	switch (dropdown_tickingscale) {
		case "s": scale = 1000; break;
		case "m": scale = 1000 * 60; break;
		case "h": scale = 1000 * 60; break;
	}
	var delay = scale / number_tickingtimes;
	var code = ScriptNode({
		name: text_scriptname,
		ticking: number_tickingtimes > 0,
		delay: delay,
		body: eval(statements_statements)
	});
	return JSON.stringify(code);
};

Blockly.JavaScript['toggle'] = function(block) {
	var dropdown_pinnumber = block.getFieldValue('pinNumber');
	var code = MessageSendNode({
		receiver: NumberNode(parseInt(dropdown_pinnumber)), 
		selector: "toggle"
	});
	return JSON.stringify(code);
};

Blockly.JavaScript['start_script'] = function(block) {
	var text_scriptname = block.getFieldValue('scriptName');
	var code = ScriptStartNode(text_scriptname);
	return JSON.stringify(code);
};

Blockly.JavaScript['toggle_variable'] = function(block) {
	var value_pinnumber = Blockly.JavaScript.valueToCode(block, 'pinNumber', Blockly.JavaScript.ORDER_ATOMIC);
	var code = MessageSendNode({
		receiver: eval(value_pinnumber), 
		selector: "toggle"
	});
	return JSON.stringify(code);
};

Blockly.JavaScript['stop_script'] = function(block) {
	var text_scriptname = block.getFieldValue('scriptName');
	var code = ScriptStopNode(text_scriptname);
	return JSON.stringify(code);
};

Blockly.JavaScript['run_script'] = function(block) {
	var text_scriptname = block.getFieldValue('scriptName');
	var code = ScriptCallNode(text_scriptname);
	return JSON.stringify(code);
};

Blockly.JavaScript['wait'] = function(block) {
	var dropdown_negate = block.getFieldValue('negate');
	var value_condition = Blockly.JavaScript.valueToCode(block, 'condition', Blockly.JavaScript.ORDER_ATOMIC);
	var code = LoopNode({
		condition: eval(value_condition), 
		negated: JSON.parse(dropdown_negate)
	});
	return JSON.stringify(code);
};

Blockly.JavaScript['turn_pin'] = function(block) {
	var dropdown_pinstate = block.getFieldValue('pinState');
	var dropdown_pinnumber = block.getFieldValue('pinNumber');
	var code = MessageSendNode({
		receiver: NumberNode(parseInt(dropdown_pinnumber)), 
		selector: dropdown_pinstate
	});
	return JSON.stringify(code);
};

Blockly.JavaScript['turn_pin_variable'] = function(block) {
	var dropdown_pinstate = block.getFieldValue('pinState');
	var value_pinnumber = Blockly.JavaScript.valueToCode(block, 'pinNumber', Blockly.JavaScript.ORDER_ATOMIC);
	var code = MessageSendNode({
		receiver: eval(value_pinnumber), 
		selector: dropdown_pinstate
	});
	return JSON.stringify(code);
};

Blockly.JavaScript['is_pin'] = function(block) {
	var dropdown_pinstate = block.getFieldValue('pinState');
	var dropdown_pinnumber = block.getFieldValue('pinNumber');
	var code = MessageSendNode({
		receiver: MessageSendNode({
			receiver: NumberNode(parseInt(dropdown_pinnumber)), 
			selector: "read"
		}),
		selector: dropdown_pinstate == "on" ? ">" : "<",
		arguments: [NumberNode(0.5)]
	});
	code = JSON.stringify(code);
	return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['is_pin_variable'] = function(block) {
	var dropdown_pinstate = block.getFieldValue('pinState');
	var value_pinnumber = Blockly.JavaScript.valueToCode(block, 'pinNumber', Blockly.JavaScript.ORDER_ATOMIC);
	var code = MessageSendNode({
		receiver: MessageSendNode({
			receiver: eval(value_pinnumber), 
			selector: "read"
		}),
		selector: dropdown_pinstate == "on" ? ">" : "<",
		arguments: [NumberNode(0.5)]
	});
	return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['read_pin'] = function(block) {
	var dropdown_pinnumber = block.getFieldValue('pinNumber');
	var code = MessageSendNode({
		receiver: NumberNode(parseInt(dropdown_pinnumber)), 
		selector: "read"
	});
	code = JSON.stringify(code);
	return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['read_pin_variable'] = function(block) {
	var value_pinnumber = Blockly.JavaScript.valueToCode(block, 'pinNumber', Blockly.JavaScript.ORDER_ATOMIC);
	var code = MessageSendNode({
		receiver: eval(value_pinnumber), 
		selector: "read"
	});
	code = JSON.stringify(code);
	return [code, Blockly.JavaScript.ORDER_NONE];
};

Blockly.JavaScript['write_pin'] = function(block) {
	var dropdown_pinnumber = block.getFieldValue('pinNumber');
	var value_pinvalue = Blockly.JavaScript.valueToCode(block, 'pinValue', Blockly.JavaScript.ORDER_ATOMIC);
	var code = MessageSendNode({
		receiver: NumberNode(parseInt(dropdown_pinnumber)), 
		selector: "write:", 
		arguments: [eval(value_pinvalue)]
	});
	return JSON.stringify(code);
};

Blockly.JavaScript['write_pin_variable'] = function(block) {
	var value_pinnumber = Blockly.JavaScript.valueToCode(block, 'pinNumber', Blockly.JavaScript.ORDER_ATOMIC);
	var value_pinvalue = Blockly.JavaScript.valueToCode(block, 'pinValue', Blockly.JavaScript.ORDER_ATOMIC);
	var code = MessageSendNode({
		receiver: eval(value_pinnumber),
		selector: "write:", 
		arguments: [eval(value_pinvalue)]
	});
	return JSON.stringify(code);
};

function ScriptNode(options) {
	return {
		type: "Script",
		name: options.name,
		ticking: options.ticking,
		delay: options.delay,
		body: options.body
	};
}

function MessageSendNode(options) {
	return {
		type: "MessageSend",
		receiver: options.receiver,
		selector: options.selector,
		arguments: options.arguments == undefined ? [] : options.arguments
	};
}

function ScriptStartNode(scriptName) {
	return {
		type: "ScriptStart",
		scriptName: scriptName
	};
}

function ScriptStopNode(scriptName) {
	return {
		type: "ScriptStop",
		scriptName: scriptName
	};
}

function ScriptCallNode(scriptName) {
	return {
		type: "ScriptCall",
		scriptName: scriptName
	};
}

function LoopNode(options) {
	return {
		type: "Loop",
		pre: options.pre,
		condition: options.condition,
		post: options.post,
		negated: options.negated
	}
}

function NumberNode(value) {
	return {
		type: "Number",
		value: value
	};
}