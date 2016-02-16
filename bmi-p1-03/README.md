Autores
========
	Guillermo Ruiz Álvarez
	Enrique Cabrerizo Fernández

La clase testIndex generará un archivo indexstats en una carpeta output/ creada
en el lugar de ejecución. Dicho archivo contiene una línea por cada término del índice en 
la que se indica:
	termino frecuencia #Documentos tf idf
El archivo inicialmente no está ordenado por frecuencia, para ordenarlo se utiliza el
script printgraphs.sh como se indica más adelante.

La clase testSearcher generará un archivo searchstats en la misma carpeta que contiene, para cada
consulta una fila con formato:
	#consulta P@5 P@10

El script printgraphs.sh recibe como argumento dicho archivo y se encarga de ordenar los
términos por frecuencia y de imprimir las gráficas usando gnuplot. Para ello se apoya de 
un archivo auxiliar que se elimina al terminar el script. Si por alguna razón se desea 
conservar dicho archivo (por ejemplo para ver la lista de términos ordenada), hay que comentar
la última línea del script.

IMPORTANTE
==========

El primer argumento de testIndex debe ser la ruta a indexar. Si el argumento es una carpeta, 
se indexará TODO el contenido de dicha carpeta, si es un archivo, se indexará solamente dicho archivo.
