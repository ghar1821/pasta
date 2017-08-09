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

#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <unistd.h>
#include <sys/stat.h>
#include <grp.h>

/* pastasandbox */
#define HELPER_ID 1001

int findgid(char *fname) {
	struct stat *buf;
	buf = malloc(sizeof(struct stat));
	
	if(stat(fname, buf) < 0) {
		(void) fprintf(stderr, "Cannot stat %s\n", fname);
		return -1;
	}
	int gid = buf->st_gid;
	
	free(buf);
	return gid;
}


main(argc,argv)
int argc;
char *argv[];
{
	if (argc <= 2)
		(void) fprintf(stderr, "Usage: %s matlab_path command args\n", argv[0]);
        else
        {
        
        int gid = findgid(argv[1]);
        
		gid_t supplementary_groups[] = {gid};
		int result = setgroups(1, supplementary_groups);
		if(result != 0) {
    		(void) fprintf(stderr, "setgroups {%d} failed with error %d\n", gid, result);
			perror("");
			exit(1);
    	}
    	
		result = setgid(HELPER_ID);
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
		execvp(argv[2], argv + 2);
		(void) fprintf(stderr, "%s: could not execvp %s. ", argv[0], argv[2]);
		perror(argv[2]);
	}
	exit(1);
}
