# IPv4 address Counter

IPv4 address Counter - консольное приложение для подсчёта количества уникальных IPv4 адресов в файле.

В качестве структуры данных для хранения адресов был выбран массив байт размером 2^32/8 = 536870912.
Каждый бит в байте - один из 2^32 возможных IPv4 адресов (первый байт в массиве - первые восемь адресов и т.д.).
Для каждого адреса вычисляется число от 0 до 2^32, уникально определяющее конкретный адрес (метод ipToLong).
Потом определяется к какому байту и какому биту байта принадлежит адрес. 
С помощью методов getBit и setBit проверяется наличие или отсутствие конкретного адреса.

Параллельное чтение и обработка строк сделана по примеру, взятому отсюда - https://gist.github.com/dfa1/65ae63ca3f10a72e57885add52c48b4f/

Расходы по памяти всегда минимум 536 Мб на хранение массива байт.

Выгода по времени по сравнению с наивным решением с помощью HashSet начинается, когда количество уникальных адресов в файле становится равно больше 1000000.

## Time Complexity
##### Характеристики моего ноута, на котором всё запускалось:
![Hard Specs](https://github.com/advolotkina/images-for-readmes/blob/master/ipv4-address-counter/hard-specs.png?raw=true)

##### Сравнение моей реализации с реализацией на основе HashMap
![Algo_Comparison](https://github.com/advolotkina/images-for-readmes/blob/master/ipv4-address-counter/algo-comparison.png?raw=true)

##### Тоже самое в таблице:
| Количество строк в файле  | Время подсчёта с помощью IPCountHashMap (сек) | Время подсчёта с помощью IPCount (сек) | Количество уникальных адресов в файле
| ------------- | ------------- | ------------- | ------------- |
| 10 | 0.00352435224 | 0.052608951259999995 | 10
| 100 | 0.00307376056 | 0.03841188486 | 100
| 1000 | 0.00338841956 | 0.037634298 | 1000
| 10000 | 0.007399511179999999 | 0.04277635378 | 10000
| 100000 | 0.04610728394 | 0.0644530027 | 99998
| 1000000 | 0.47281392056 | 0.26404371228 | 999885
| 10000000 | 14.36851194854 | 2.27462048522 | 9988331

С количеством адресов в файле = 100000000, HashMap переставала влезать в память и вылетала Out Of Memory.
У меня не получилось оптимизировать алгоритм с использованием HashMap/HashSet, хоть я и пыталась, поэтому я выбрала массив байт.

Большой файл (ip_addresses на 120 ГБ из описания задачи) мой алгоритм обработал за ~25 минут на моём ноутбуке. 
Ноутбук в это время был подключен к питанию и не занимался какими-то другими "тяжелыми" задачами.

## Usage
```bash
source /etc/profile.d/gradle.sh
gradle build
java -jar ./ipv4_address_counter-master/build/libs/ip_counter-1.0-SNAPSHOT.jar {file_path}
```
Или открыть проект в IDE и создать экземпляр класса IPCount:
```java
IPCount ipCount = new IPCount(filePath);
```

## Feedback
Посредственно знаю Java, буду рада любому фидбэку!

## License
[MIT](https://choosealicense.com/licenses/mit/)