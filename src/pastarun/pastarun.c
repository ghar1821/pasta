/*
 *	Run a command as pasta's Little Helper to avoid vandalism.
 *	Jason Catlett    Tue May 15 17:10:42 AEST 1984
 *	Based on an idea by Chris Maltby.
 *
 *	Ported to the MIPS - Tue Feb 14 10:43:25 EST 1989
 *              - sysmess replaced with perror
 *	Ported to Solaris 2.x - Tue Dec  7 11:28:06 EST 1993
 *	Ported to Linux 2.x - Fri Mar 24 17:02:15 AEDT 2017
 *
 *			Greg Ryan
 * 
 */

#include	<stdlib.h>
#include        <stdio.h>
#include	<sys/types.h>
#include        <unistd.h>

/* pastasandbox */
#define HELPER_ID 1001

main(argc,argv)
int argc;
char *argv[];
{
	if (argc == 1)
		(void) fprintf(stderr, "Usage: %s command args\n", argv[0]);
        else
        {
		int result = setgid(HELPER_ID);
		if (result != 0)
		{
			(void) fprintf(stderr, "setgid %d failed with error %d\n", HELPER_ID, result);
			perror("");
			exit(1);
		}
		if (setuid(HELPER_ID) != 0)
		{
			(void) fprintf(stderr, "setuid %d failed\n", HELPER_ID);
			perror("");
			exit(1);
		}
		execvp(argv[1], argv + 1);
		(void) fprintf(stderr, "%s: could not execvp %s. ", argv[0], argv[1]);
		perror(argv[1]);
	}
	exit(1);
}
