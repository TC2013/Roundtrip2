#include <stdio.h>
#include <stdlib.h>
#include <string.h>
int main(int argc, char* argv[]) {
  if (argc < 2) {
    fprintf(stderr,"Need filename");
    exit(1);
  }
  char * filename = argv[1];

  FILE * f = fopen(filename,"r");
  if (!f) {
    perror(0);
    exit(2);
  }

  unsigned char buf[32];
  int nread;
  int done = 0;
  int i;
  while (!done) {
    nread = fread(buf,32,1,f);
    for (i=0; i<nread*32; i++) {
      printf("%02X ",buf[i]);
    }
    printf("\n");
    done = feof(f);
  }
}
