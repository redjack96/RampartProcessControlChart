# RampartProcessControlChart
## Rossi Giacomo Lorenzo - 0292400
##### Deliverable 1 - ISW2 2020-2021

Il progetto consiste nel ricavare informazioni dalla piattaforma Jira per poter valutare la
stabilità del numero di ticket Jira risolti per il progetto Apache Ramport, attraverso un Process Control Chart.


Nota: Su Jira era presente il repository del progetto RAMPART, gestito da SVN, ma sono riuscito a trovare un Mirror GitHub 
per utilizzare i comandi di git attraverso la libreria JGit. È presente anche una classe che utilizza la libreria SVNKit per ricavare 
la storia presente nella repo SVN. Purtroppo i contenuti della svn history sono un sottoinsieme della git history, quindi mi baso principalmente sui commit di git.

---

## Struttura del deliverable
La repository contiene un progetto Maven che include le dipendenze per JSON e le configurazioni per SonarCloud.
L'analisi di SonarCloud è stata impostata tramite github action e avviene a ogni commit/pull-request.
Per il progetto SonarCloud collegato a questa repository cliccare [qui](https://sonarcloud.io/dashboard?id=redjack96_RampartProcessControlChart).

Sono inclusi nella repository
- il codice senza code-smells
- vari csv prodotti dalle repository Git, SVN e da Jira
- il file excel contenente i Process-Control-Chart dei fixed ticket per il progetto Apache Rampart
- il documento PowerPoint che contiene la relazione del deliverable
