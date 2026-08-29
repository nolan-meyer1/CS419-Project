//A simplified Unix-style shell that supports up to one pipe

#include <stdio.h>
#include <string.h>
#include <strings.h>  //for strcasecmp()
#include <unistd.h>
#include <sys/wait.h>

int main(){
  while(1){
    //print prompt
    printf("CS419Shell> ");
    fflush(stdout);

    //read a line of user input
    char input[1024];
    if(fgets(input, sizeof(input), stdin)==NULL){
      printf("Couldn't read user input.\n");
      break;
    }

    //remove the trailing newline character
    input[strcspn(input, "\n")] = '\0';
    if(input[0] == '\0'){
      //the input was empty (user just pressed Enter)
      continue;
    }

    //we accept at most this many arguments
    // (the command itself is considered the first argument)
    int MAX_NUM_ARGS = 5;
    
    // this is effectively an array of strings
    char *args[MAX_NUM_ARGS+1];

    //Split the input string by space,
    //and return just the first token.
    //(Subseqent calls to strtok return the next tokens.)
    char *token = strtok(input, " ");

    //track the number of arguments we've taken in so far
    int i=0; 

    //Take in all tokens
    for( ; token!=NULL && i<MAX_NUM_ARGS; i++){
      //quote stripping: remove one leading and trailing quote if present
      int len = strlen(token);
      if (len >= 2 && 
       ((token[0] == '\'' && token[len-1] == '\'') ||
        (token[0] == '"'  && token[len-1] == '"'))) {
        token[len-1] = '\0';//cut off last quote
        token++;            //a hacky way to skip first quote
      }
      args[i] = token;
      //get the next token from the same input string
      token = strtok(NULL, " ");
    }
    //execvp requires the last array element to be NULL
    args[i] = NULL;

    //check for "quit" or "exit" (case insensitive)
    if (args[0]!=NULL && (strcasecmp(args[0], "exit")==0 ||
			  strcasecmp(args[0], "quit") == 0)){
      printf ("Exiting...\n");
      break;
    }

    //detect a single pipe '|' (require spaces around it)
    int pipe_pos = -1;
    int pipe_num = 0;
    for (int j = 0; args[j] != NULL; j++){     
      if (strcmp(args[j], "|") == 0){
	  pipe_pos = j;
	  pipe_num++;
      }
    }

    if(pipe_num > 1){
      //user input contains multiple |
      printf("This shell only supports a single pipe.\n");
      continue;
    }
    
    if(pipe_num == 0){
      //no pipe operator, single command

      //print out the command and args for debugging 
      for (int j = 0; args[j] != NULL; j++){
	printf("arg[%d] = '%s'\n", j, args[j]);
      }

      //TODO: create a child process to execute the command
      int p = fork();
      if(p == 0){
        execvp(args[0],args); 
      }else{
        waitpid(p, NULL, 0);
      } 
      
    }

    if(pipe_num == 1){
      //user input contains a single pipe
      if (pipe_pos==0 || args[pipe_pos+1]==NULL){
	//the pipe operator must not be the first or last token
	printf("| syntax error\n");
	continue;
      }

      //set up the args for the two commands
      char *args_1[MAX_NUM_ARGS+1];
      char *args_2[MAX_NUM_ARGS+1];
      int k=0;
      for ( ; k<pipe_pos; k++){ 
	args_1[k] = args[k];       
      }                                     
      args_1[k] = NULL;
      k++; //skip the | character
      int h=0;
      for( ; args[k]!=NULL; h++,k++){
	args_2[h] = args[k];
      }
      args_2[h] = NULL;

      //print out the command and args for debugging 
      for (int j = 0; args_1[j] != NULL; j++){
	printf("args_1[%d] = '%s'\n", j, args_1[j]);
      }
      for (int j = 0; args_2[j] != NULL; j++){
	printf("args_2[%d] = '%s'\n", j, args_2[j]);
      }

     //TODO: 
     //(1) create a pipe
     //(2) create two child processes to execute the two commands
     //(3) The parent process should close both ends of the pipe and wait for both children
     //system calls you will need: pipe(), fork(), dup2(), execvp(), wait()/waitpid()

      
    }//if 
  }//while
  return 0;
}//main
    
