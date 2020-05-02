as yet unclassified
write: assertions named: testName on: out 
	out nextPutAll: '(deftest '.
	out nextPutAll: testName capitalized; crlf.
	(assertions select: [:each | each key isString])
		do: [:assertion || src valid |
			src := assertion key.
			valid := assertion value.
			out nextPutAll: ('  (is ({1}? "{2}"))' format: { 
				valid ifTrue: ['valid'] ifFalse: ['invalid'].
				self formatSrc: src.
				(src copyReplaceAll: {Character cr} with: String crlf)
					copyReplaceAll: '"' with: '\"'
			})]
		separatedBy: [out crlf].
	out nextPutAll: ')'; crlf; crlf