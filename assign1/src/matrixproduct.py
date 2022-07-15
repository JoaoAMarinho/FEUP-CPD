import time

def OnMult(size):
    lin = col = size

    matrix_a = [[1.0 for _ in range(lin)] for _ in range(lin)]
    matrix_b = [[x+1 for _ in range(col)] for x in range(col)]
    matrix_c = [[0 for _ in range(lin)] for _ in range(lin)] 

    start = time.time()
    
    for i in range(lin):
        for j in range(col):
            temp = 0
            for k in range(lin):
                temp += matrix_a[i][k]*matrix_b[k][j]
            matrix_c[i][j] = temp

    end = time.time()
    return "{:.5f}".format(end - start)
    

def OnMultLine(size):
    lin = col = size

    matrix_a = [[1.0 for _ in range(lin)] for _ in range(lin)]
    matrix_b = [[x+1 for _ in range(col)] for x in range(col)]
    matrix_c = [[0 for _ in range(lin)] for _ in range(lin)] 

    start = time.time()
    
    for i in range(lin):
        for k in range(col):
            value = matrix_a[i][k]
            for j in range(lin):
                matrix_c[i][j] += value*matrix_b[k][j]

    end = time.time()

    return "{:.5f}".format(end - start)

def OnMultBlock(size):
    lin = col = size

    blockSize = int(input('BlockSize? '))

    matrix_a = [1.0 for _ in range(lin) for _ in range(lin)]
    matrix_b = [x+1 for x in range(col) for _ in range(col)]
    matrix_c = [0 for _ in range(lin) for _ in range(lin)] 
    
    start = time.time()
    
    for i in range(0, lin, blockSize):
        for j in range(0, col, blockSize):
            for k in range(0, lin, blockSize):   
     
                for l in range(i, min(i + blockSize, lin)):
                    for m in range(k, min(k + blockSize, lin)):
                        for n in range(j, min(j + blockSize, lin)):                            
                            # bkPos_ij = lin * (i * blockSize + l) + j * blockSize + n
                            # bkPos_ik = lin * (i * blockSize + l) + k * blockSize + m
                            # bkPos_kj = lin * (k * blockSize + m) + j * blockSize + n
                            matrix_c[lin*l + n] += matrix_a[lin*l + m]*matrix_b[lin*m + n]

    end = time.time()

    return "{:.5f}".format(end - start)


def main():
    op = 1
    while op != 0:
        print("1. Multiplication")
        print("2. Line Multiplication")
        print("3. Block Multiplication")
        op = int(input("Selection?: "))
        if op == 0:
            break

        # match op:
        #     case 1:
        #         func = OnMult
        #     case 2:
        #         func = OnMultLine
        #     case 3:
        #         func = OnMultBlock
        #     case True:
        #         continue

        func = OnMult
        
        for i in range(600,3000,400):
            res = func(i)
            print("finised", i, res)
             
if __name__ == "__main__":
    main()