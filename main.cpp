#include <conio.h>
#include <stdio.h>
#include <ctype.h>
#define MAXPILHA 301

struct tppilha
{
	int topo;
	char pilha[MAXPILHA];
};
struct tparquivo
{
    int n;
    char matriz[1000][MAXPILHA];
};
void inicializa(tppilha &p)
{
	p.topo=-1;
}
char vazia(int topo)
{
    return topo == -1;
}
char cheia(int topo)
{
    return topo + 1 == MAXPILHA;
}
void insere(tppilha &p, char elemento)
{
    p.pilha[++p.topo] = elemento;
}
char retira(tppilha &p)
{
    return p.pilha[p.topo--];
}
char elementotopo(tppilha p)
{
    return p.pilha[p.topo];
}
void exibepilha(tppilha p)
{
    while(!vazia(p.topo))
        printf("%c",retira(p));
}
void learquivo(tparquivo &arquivo)
{
    FILE *arq=fopen("Infixa.txt","r");
    if(arq)
    {
        arquivo.n = fgetc(arq) - 48;
        fgetc(arq);
        for(int i=0; i < arquivo.n; i++)
            fgets(arquivo.matriz[i],MAXPILHA,arq);
        fclose(arq);
    }
}
int prioridade(char a, char b)
{
    int A, B;
    switch(a)
    {
        case '-':
        case '+': A=1; break;
        case '/':
        case '*': A=2; break;
        case '^': A=3; break;
        case '(':
        case ')': A=0; break;
    }
    switch(b)
    {
        case '-':
        case '+': B=1; break;
        case '/':
        case '*': B=2; break;
        case '^': B=3; break;
        case '(':
        case ')': B=0; break;
    }
    if(A > B)
        return 1;
    else if(A < B)
        return -1;
    else
        return 0;
}
void criaarquivo(tparquivo aux)
{
    char linha[MAXPILHA];
    int l=0;
    FILE *arq=fopen("Posfixa.txt","w");
    while(l<aux.n)
        fputs(aux.matriz[l++],arq);
    fclose(arq);

}
void operador(tppilha &p, char caractere, tparquivo &aux, int l, int &c2)
{
	char maior;
	if(!vazia(p.topo))
    {
        if(prioridade(elementotopo(p),caractere) < 0)
        {
            maior = retira(p);
            insere(p,caractere);
            insere(p,maior);
		}
		else// if(!prioridade(elementotopo(p),caractere) || prioridade(elementotopo(p),caractere)<0)
		{
			while(!vazia(p.topo))
                aux.matriz[l][c2++]=retira(p);
            insere(p,caractere);
		}
            
    }
    else
        insere(p,caractere);
}
int main()
{
    tparquivo arquivo, aux;
    tppilha p;
    char caractere;
    inicializa(p);
    learquivo(arquivo);
    for(int l=0, c, c2; l < arquivo.n; l++)
    {
        c2=c=0;
        aux.n=arquivo.n;
        caractere = arquivo.matriz[l][c++];
        while(caractere != 10)
        {
            if(caractere == '+' || caractere == '-' || caractere == '*' || caractere == '/' || caractere == '^')
            {
                operador(p,caractere,aux,l,c2);
            }
            else if(caractere == '(')
            {
            	caractere = arquivo.matriz[l][c++];
                while(caractere != ')')
                {
                	if(toupper(caractere) >= 'A' && toupper(caractere) <='Z' || caractere >= '0' && caractere <='9')
                		aux.matriz[l][c2++] = caractere;
                	else if(caractere == '+' || caractere == '-' || caractere == '*' || caractere == '/' || caractere == '^')
                		operador(p,caractere,aux,l,c2);
                    caractere = arquivo.matriz[l][c++];
                }
                while(!vazia(p.topo))
                    aux.matriz[l][c2++]=retira(p);
                    
            }
            else // if(toupper(caractere) >= 'A' && toupper(caractere) <='Z' || caractere >= '0' && caractere <='9')
            {
                aux.matriz[l][c2++] = caractere;
            }
            caractere = arquivo.matriz[l][c++];
        }
        while(!vazia(p.topo))
            aux.matriz[l][c2++]=retira(p);
    }
    criaarquivo(aux);
	return 0;
}
