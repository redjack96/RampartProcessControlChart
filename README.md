# RampartProcessControlChart
##### Deliverable 1 - ISW2 2020-2021

Il progetto consiste nel ricavare informazioni dalla piattaforma Jira per poter valutare la
stabilità del numero di ticket Jira risolti per il progetto Apache Ramport, attraverso un Process Control Chart.

---

![Process-Control-Chart-Rampart](pcc.png)
> Dal grafico è possibile notare come il numero di fixed ticket 
> sono all'interno della media eccetto che nel mese di dicembre 2010, 
> durante il quale sono stati risolti ben 66 ticket. Questo è probabilmente dovuto al fatto che
> nel periodo precedente sono state implementate nuove feature che hanno introdotto un elevato 
> numero di bug. Un altro possibile motivo è che in quel mese il team di sviluppo abbia deciso di
> dedicarsi al fixing dei bug del progetto, per poterne migliorare la qualità. Il lower bound è stato 
> portato a 0 perché un numero negativo di ticket risolti non ha senso.

Per raggruppare il numero di ticket per ogni mese è stata utilizzata la funzionalità Excel "Tabella Pivot".
Dal grafico sono stati esclusi i mesi che avevano un numero di ticket risolti pari a zero.
---

## Struttura del deliverable
La repository contiene un progetto Maven che include le dipendenze per JSON e le configurazioni per SonarCloud.
L'analisi di SonarCloud è stata impostata tramite github action e avviene a ogni commit/pull-request
Il link per il progetto SonarCloud collegato a questa repository è il seguente

    https://sonarcloud.io/dashboard?id=redjack96_RampartProcessControlChart

Sono inclusi nella repository
- il codice senza code-smells
- il csv ricavato a partire dall'output del progetto
- il file excel contenente il Process-Control-Chart dei fixed ticket per il progetto Apache Rampart

