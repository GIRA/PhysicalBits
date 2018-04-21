tests
test058InvalidBinarySelectors
	| pp |
	pp := parser expression end.
	#($[ $] $( $) ${ $} $" $. $' $: $# $, $; $_)
		do: [:op || src |
			src := 'a {1} b' format: { op }.
			self assert: (pp parse: src) isPetitFailure
				description: ('Expression: ''{1}'' should fail to parse!' format: { src }).
			
			src := '3{1}b' format: { op }.
			self assert: (pp parse: src) isPetitFailure
				description: ('Expression: ''{1}'' should fail to parse!' format: { src }).
			
			src := '3 {1} 4' format: { op }.
			self assert: (pp parse: src) isPetitFailure
				description: ('Expression: ''{1}'' should fail to parse!' format: { src }).
			
			src := 'a {1}4' format: { op }.
			self assert: (pp parse: src) isPetitFailure
				description: ('Expression: ''{1}'' should fail to parse!' format: { src })]