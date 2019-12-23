private
append: msgType msg: formatString args: argsArray
	| args |
	args := argsArray collect: #asString.
	formatString lines do: [:line | buffer add: msgType -> {line . args}]