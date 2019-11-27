#include <bits/stdc++.h>
struct TpPilha
{
    int fim;
    char pilha[350];
};
void insere(TpPilha &p, char elem)
{
    p.pilha[++p.fim] = elem;
}
char retira(TpPilha &p)
{
    return p.pilha[p.fim--];
}
char topo(TpPilha p)
{
    return p.pilha[p.fim];
}
char prioridade(char a, char b)
{
    char A, B;
    switch(a)
    {
        case'(':
            A = 1;
        break;
        case'+':
        case'-':
            A = 2;
        break;
        case'*':
        case'/':
            A = 3;
        break;
        case'^':
            A = 4;
        break;
    }
    switch(b)
    {
        case'(':
            B = 1;
        break;
        case'+':
        case'-':
            B = 2;
        break;
        case'*':
        case'/':
            B = 3;
        break;
        case'^':
            B = 4;
        break;
    }
    return A-B;
}
int main()
{
    int n;
    char infixa[350];
    TpPilha pilha;
    scanf("%d%*c",&n);
    while(n--)
    {
        pilha.fim = -1; /// init_pilha
        gets(infixa);
        for(int i = 0; infixa[i] != '\0'; i++)
        {
            if((toupper(infixa[i]) < 'A' || toupper(infixa[i]) > 'Z') && (infixa[i] < '0' || infixa[i] > '9'))
            {
            	if(infixa[i] == ')')
            	{
            		while(pilha.fim > -1 && topo(pilha) != '(')
       					printf("%c",retira(pilha));
    				retira(pilha);
				}
				else
				{
					if(infixa[i] != '(' && pilha.fim > -1)
	                {
	                    while(pilha.fim > -1 && prioridade(topo(pilha),infixa[i]) >= 0)
	                        printf("%c",retira(pilha));
	                }
	                insere(pilha,infixa[i]);
				}
            }
            else
                printf("%c",infixa[i]);
        }
        while(pilha.fim > -1)
            printf("%c",retira(pilha));
        printf("\n");
    }
    return 0;
}
