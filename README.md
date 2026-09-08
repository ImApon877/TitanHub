# TitanHub

Hub core configurabile per server Bukkit, Spigot e Paper. TitanHub gestisce hotbar, navigatore modalita, protezioni hub, doppio salto, launch pad, Enderbutt, PvP controllato, scoreboard, tablist e collegamenti a proxy o comandi esterni.

## Compatibilita

- Bukkit, Spigot e Paper dalla 1.8.8 alle versioni moderne.
- Java 8 per compilare il progetto.
- Java richiesto dalla versione del server per eseguire il JAR.
- I materiali legacy e moderni piu comuni vengono convertiti automaticamente, ad esempio `WOOD_SWORD`/`WOODEN_SWORD`, `SKULL_ITEM`/`PLAYER_HEAD` e `WATCH`/`CLOCK`.
- PlaceholderAPI e opzionale. Se installata, i placeholder sono disponibili in messaggi, GUI, scoreboard e tablist.

## Installazione

1. Compila con `mvn clean package` oppure usa il file `target/TitanHub.jar` gia generato.
2. Copia `TitanHub.jar` nella cartella `plugins` del server.
3. Avvia il server una prima volta per creare `plugins/TitanHub/config.yml` e `messages.yml`.
4. Entra nel server e usa `/sethub` nel punto desiderato.
5. Riavvia il server oppure usa `/titanhub reload` dopo le modifiche ai file YAML.

TitanHub aggiunge automaticamente al file di configurazione esistente le nuove chiavi mancanti, senza sovrascrivere i valori gia personalizzati.

## Comandi e permessi

| Comando | Descrizione | Permesso |
| --- | --- | --- |
| `/hub` | Teletrasporta il giocatore all'hub. | Nessuno |
| `/sethub` | Salva posizione, rotazione e mondo dell'hub. | `titanhub.admin` |
| `/titanhub reload` | Ricarica configurazione, messaggi, hotbar, scoreboard e tablist. | `titanhub.admin` |

`titanhub.admin` e assegnato agli OP per default. Gli admin ignorano le principali protezioni dell'hub.

## Hotbar

Gli oggetti vengono dati al login e dopo il respawn. Non possono essere spostati, trascinati, scambiati, droppati o persi alla morte.

| Oggetto | Configurazione | Funzione |
| --- | --- | --- |
| Player Hider | `items.player-hider` | Mostra o nasconde gli altri giocatori. Cooldown predefinito: 3 secondi. |
| Spada PvP | `items.pvp` | Attiva la modalita PvP dopo 3 secondi. |
| Selettore modalita | `items.navigator` | Apre il menu con le destinazioni configurate. |
| Enderbutt | `items.enderbutt` | Lancia il giocatore nella direzione guardata. Rimane nell'ultimo slot. |

Il vecchio profilo e disattivato per default e non viene inserito nella hotbar.

## Navigatore modalita

La GUI e definita in `navigator`.

```yml
navigator:
  title: '&8Selettore modalita'
  size: 27
  decoration:
    enabled: true
    material: CYAN_STAINED_GLASS_PANE
    data: 9
    name: '&r '
    slots: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26]
  destinations:
    lifesteal:
      slot: 12
      material: PLAYER_HEAD
      head-owner: MHF_Heart
      name: '&cLifesteal'
      lore:
        - '&7Entra nel server Lifesteal.'
      action: SERVER
      value: lifesteal
```

Ogni destinazione supporta:

| Chiave | Descrizione |
| --- | --- |
| `slot` | Posizione nella GUI, da 0 in avanti. |
| `material` | Materiale dell'icona. Usa `PLAYER_HEAD` per una testa. |
| `head-owner` | Nome proprietario della testa, opzionale. |
| `name` | Nome dell'icona. Supporta `&` per i colori. |
| `lore` | Lista delle righe descrittive. |
| `action` | `SERVER` oppure `COMMAND`. |
| `value` | Nome server per `SERVER`; comando senza `/` per `COMMAND`. |

### Proxy e AJQueue

Per BungeeCord usa `SERVER`:

```yml
action: SERVER
value: lifesteal
```

Il backend deve avere il canale BungeeCord disponibile e `lifesteal` deve corrispondere al nome nel proxy.

Per plugin coda come AJQueue usa `COMMAND`:

```yml
action: COMMAND
value: 'ajqueue join lifesteal'
```

TitanHub esegue il comando come giocatore. Controlla la sintassi richiesta dalla versione di AJQueue installata.

## PvP

Tieni in mano la spada PvP per iniziare il countdown `3`, `2`, `1`. Al termine ricevi:

- Spada in ferro indistruttibile.
- Set completo in pelle indistruttibile.
- Protection I su gambali e stivali.

Nel mondo hub:

- Un giocatore senza PvP attivo non puo danneggiare player, mob o altre entita.
- Per danneggiare un player, entrambi devono avere il PvP attivo.
- Dopo un combattimento, il PvP non puo essere disattivato per 10 secondi.
- Per uscire dal PvP cambia slot o prova a togliere la spada: parte un countdown di 3 secondi.
- Se riporti la spada PvP nello slot prima della fine, l'uscita viene annullata.
- La morte disattiva il PvP.

Configurazione:

```yml
items:
  pvp:
    enabled: true
    slot: 1
    material: IRON_SWORD
    name: '&cPvP &7(Tieni in mano)'
    lore:
      - '&7Attiva il PvP dopo 3 secondi.'
```

## Movimento e protezioni

`settings` controlla le protezioni applicate al mondo dell'hub:

- Danni, fame, drop, rottura e piazzamento blocchi.
- Interazioni con blocchi ed entita, secchi, fuoco, propagazione ed esplosioni.
- Meteo.
- Respawn sempre alla posizione impostata con `/sethub`.

Altre funzioni configurabili:

```yml
double-jump:
  enabled: true
  strength: 1.1
  sound: BAT_TAKEOFF

launch-pads:
  enabled: true
  material: GOLD_PLATE
  strength: 1.5
  vertical-boost: 1.0
  sound: BAT_TAKEOFF

enderbutt:
  enabled: true
  strength: 1.35
  vertical-boost: 0.55
  cooldown-millis: 350
  sound: ENDERMAN_TELEPORT
```

L'Enderbutt mantiene la direzione verticale del giocatore: mirando in basso non spinge artificialmente verso l'alto.

## Scoreboard e tablist

Entrambe sono configurabili in `settings`.

```yml
settings:
  scoreboard:
    enabled: true
    title: '&8[&bTitan&3Hub&8]'
    lines:
      - '&7Giocatore'
      - '&f%player%'
      - '&7Online: &b%online%'
  tab:
    enabled: true
    header:
      - '&bTitanHub'
      - '&7Benvenuto, &f%player%'
    footer:
      - '&7Giocatori online: &b%online%'
      - '&etitanhub.example'
```

Il Player Hider usa l'API Bukkit `hidePlayer`: il giocatore nascosto non e visibile nel mondo e, sui server moderni, anche nella tablist del giocatore che ha attivato il filtro.

## Messaggi

Tutti i messaggi sono in `messages.yml`. Colori legacy con `&` e PlaceholderAPI sono supportati.

Messaggi PvP disponibili:

- `pvp-countdown`
- `pvp-enabled`
- `pvp-cancelled`
- `pvp-in-combat`
- `pvp-exit-countdown`
- `pvp-disabled`
- `pvp-reentry-cooldown`

## Compilazione

Prerequisiti: JDK 8 o successivo e Maven.

```bash
mvn clean package
```

Il plugin compilato viene creato in `target/TitanHub.jar`.

## Struttura progetto

```text
TitanHub/
├── src/main/java/me/imapon/titanhub/TitanHub.java
├── src/main/resources/config.yml
├── src/main/resources/messages.yml
├── src/main/resources/plugin.yml
├── pom.xml
├── README.md
└── .gitignore
```
