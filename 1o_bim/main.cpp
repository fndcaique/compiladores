#include<bits/stdc++.h>

int main(){
	FILE *f = fopen("out.txt", "w");
	for(char c = 'A'; c <= 'Z' ; ++c){
		fprintf(f,"%c, ",c);
	}
	fclose(f);
}