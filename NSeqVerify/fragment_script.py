import random

NUM_FRAGMENTOS = 75000
LONGITUD_FRAGMENTO = 150
ARCHIVO_SALIDA = ""

secuencia = ""

try:

    with open(ARCHIVO_SALIDA, "w", encoding="utf-8") as archivo:

        print(f"Generando {NUM_FRAGMENTOS} fragmentos en el archivo '{ARCHIVO_SALIDA}'...")

        longitud_total = len(secuencia)
        indice_maximo_de_inicio = longitud_total - LONGITUD_FRAGMENTO

        if indice_maximo_de_inicio < 0:

            print("Error: La secuencia de ADN es más corta que la longitud del fragmento deseado.")

        else:

            for _ in range(NUM_FRAGMENTOS):

                inicio = random.randint(0, indice_maximo_de_inicio)

                fin = inicio + LONGITUD_FRAGMENTO
                fragmento = secuencia[inicio:fin]

                archivo.write(fragmento + "\n")

            print("¡Proceso completado con éxito!")

except IOError as e:

    print(f"Error al escribir en el archivo: {e}")

except Exception as e:

    print(f"Ha ocurrido un error inesperado: {e}")
