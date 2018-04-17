private
primSpec
	"{ name . code . stackTransition }"
	^ {
		{#read . 16r0 . 1 -> 1}.
		{#write . 16r1 . 2 -> 0}.
		{#toggle . 16r2 . 1 -> 0}.
		{#servoDegrees . 16r3 . 2 -> 0}.
		{#servoWrite . 16r4 . 2 -> 0}.
		{#multiply . 16r5 . 2 -> 1}.
		{#add . 16r6 . 2 -> 1}.
		{#divide . 16r7 . 2 -> 1}.
		{#subtract . 16r8 . 2 -> 1}.
		{#seconds . 16r9 . 0 -> 1}.
		{#equals . 16rA . 2 -> 1}.
		{#notEquals . 16rB . 2 -> 1}.
		{#greaterThan . 16rC . 2 -> 1}.
		{#greaterThanOrEquals . 16rD . 2 -> 1}.
		{#lessThan . 16rE . 2 -> 1}.
		{#lessThanOrEquals . 16rF . 2 -> 1}.
		{#negate . 16r10 . 1 -> 1}.
		{#sin . 16r11 . 1 -> 1}.
		{#cos . 16r12 . 1 -> 1}.
		{#tan . 16r13 . 1 -> 1}.
		{#turnOn . 16r14 . 1 -> 0}.
		{#turnOff . 16r15 . 1 -> 0}.
		{#yield . 16r16 . 0 -> 0}.
		{#yieldTime . 16r17 . 1 -> 0}.
		{#millis . 16r18 . 0 -> 1}.
		{#ret . 16r19 . 0 -> 0}.
		{#pop . 16r1A . 1 -> 0}.
		{#retv . 16r1B . 1 -> 0}.
		{#coroutine . 16r1C . 0 -> 1}.
		{#logicalAnd . 16r1D . 2 -> 1}.
		{#logicalOr . 16r1E . 2 -> 1}.
		{#bitwiseAnd . 16r1F . 2 -> 1}.
		{#bitwiseOr . 16r20 . 2 -> 1}.
		{#serialWrite . 16r21 . 1 -> 0}
	}