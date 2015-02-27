gll:
	@if [ -f *.c ]; then \
		gcc *.c -o MyProgram; \
	fi
	@if [ -f *.c* ] && [ ! -f MyProgram ]; then \
		g++ *.c* -o MyProgram; \
	fi