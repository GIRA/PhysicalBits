as yet unclassified
writeCLJ: program encoded: bytes testname: name
"
(deftest {1}  
  (let [expected {2}
        program {3}
        actual (en/encode program)]
    (is (= expected actual))))
"
	| clj template |
	template := '(deftest {1}  
  (let [expected [{2}]
        program {3}
        actual (en/encode program)]
    (is (= expected actual))))' copyReplaceAll: {Character cr} with: String crlf.

	clj := String streamContents: [:stream || index |
		index := 0.
		program cljString linesDo: [:line |
			index = 0 ifFalse: [stream crlf.
				8 timesRepeat: [stream space]].
			stream nextPutAll: line.
			index := index + 1]].
	
	out nextPutAll: (template format: { name capitalized . (String streamContents: [:s | bytes do: [:b | s print: b] separatedBy: [s space]]) . clj}).
	out  crlf; crlf.
		