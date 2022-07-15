#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <papi.h>
#include <algorithm>    // std::min
#include <cstring>

using namespace std;

#define SYSTEMTIME clock_t

int blockSize=128;

void OnMult(int m_ar, int m_br) 
{
	
	SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
	

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);



    Time1 = clock();

	for(i=0; i<m_ar; i++)
	{	for( j=0; j<m_br; j++)
		{	temp = 0;
			for( k=0; k<m_ar; k++)
			{	
				temp += pha[i*m_ar+k] * phb[k*m_br+j];
			}
			phc[i*m_ar+j]=temp;
		}
	}


    Time2 = clock();
	sprintf(st, "Time: %3.3f ", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

    free(pha);
    free(phb);
    free(phc);
	
}

// add code here for line x line matriz multiplication
void OnMultLine(int m_ar, int m_br)
{
 	
	SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
	

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	memset(phc, 0, (m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);



    Time1 = clock();

	for(i=0; i<m_ar; i++)
	{	for(k=0; k<m_br; k++)
		{
			for( j=0; j<m_ar; j++)
			{	
				phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
			}
		}
	}

    Time2 = clock();
	sprintf(st, "Time: %3.3f ", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

    free(pha);
    free(phb);
    free(phc);
	   
}

void OnMultBlock(int m_ar, int m_br)
{
    // Blocks of 128, 256, 512
	SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k, l, m, n;
	int bkPos_ij, bkPos_ik, bkPos_kj;

	double *pha, *phb, *phc;
	

		
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	memset(phc, 0, (m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);



    Time1 = clock();

	for(i=0; i<m_ar; i += blockSize)
	{
		for(j=0; j<m_br; j += blockSize)
		{
			for(k=0; k<m_ar; k += blockSize)
			{
				for(l = i; l<min(i + blockSize, m_ar); l++)
				{
					for(m = k; m<min(k + blockSize, m_br); m++)
					{
						for (n = j; n<min(j + blockSize, m_ar); n++)
						{
							phc[m_ar*l + n] += pha[m_ar*l + m] * phb[m_ar*m + n];
						}
					}
				}
			}
		}
	}

    Time2 = clock();
	sprintf(st, "Time: %3.3f ", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

    free(pha);
    free(phb);
    free(phc);

}



void handle_error (int retval)
{
  printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
  exit(1);
}

void init_papi() {
  int retval = PAPI_library_init(PAPI_VER_CURRENT);
  if (retval != PAPI_VER_CURRENT && retval < 0) {
    printf("PAPI library version mismatch!\n");
    exit(1);
  }
  if (retval < 0) handle_error(retval);

  std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
            << " MINOR: " << PAPI_VERSION_MINOR(retval)
            << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}


int main (int argc, char *argv[])
{

	char c;
	int lin, col;
	int op;
	
	int EventSet = PAPI_NULL;
  	long long values[2];
  	int ret;
	

	ret = PAPI_library_init( PAPI_VER_CURRENT );
	if ( ret != PAPI_VER_CURRENT )
		std::cout << "FAIL" << endl;


	ret = PAPI_create_eventset(&EventSet);
		if (ret != PAPI_OK) cout << "ERROR: create eventset" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCM" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCM" << endl;


	op=1;
	do {
		cout << endl << "1. Multiplication" << endl;
		cout << "2. Line Multiplication" << endl;
		cout << "3. Block Multiplication" << endl;
		cout << "Selection?: ";
		cin >>op;
		if (op == 0)
			break;

		void (*func)(int, int);

		switch (op){
			case 1:
				func = &OnMult;
				break;
			case 2:
				func = &OnMultLine;  
				break;
			case 3:
				cout << "Block Size? ";
				cin >> blockSize;
				func = OnMultBlock;  
				break;

		}

		
		for (int i=4096; i<=10240; i+=2048){
			// Start counting
			cout << "Size: "  << i << " ";
			ret = PAPI_start(EventSet);
			if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

			(*func)(i, i);

			ret = PAPI_stop(EventSet, values);
			if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
			printf("L1 DCM: %lld ",values[0]);
			printf("L2 DCM: %lld \n",values[1]);

			ret = PAPI_reset( EventSet );
			if ( ret != PAPI_OK )
				std::cout << "FAIL reset" << endl; 
		}

	}while (op != 0);

	ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_destroy_eventset( &EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL destroy" << endl;

}