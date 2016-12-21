read/write
xor: data with: mask
	"XOR all the data using the given mask bytes.

	WARNING: Does inplace reversal; do not call with literal data."

	"This would be much faster w/ BitBlt but data may not be 
	4-byte aligned and so BB will barf."
	1 to: data size do:[:i|
		data byteAt: i put: ((data byteAt: i) bitXor: (mask byteAt: i-1 \\ 4 + 1)).
	].
