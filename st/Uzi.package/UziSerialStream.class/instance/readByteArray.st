reading
readByteArray
	^ [port readByteArray]
		on: Error
		do: [:ign | #[]]