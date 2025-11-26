# Testing BDI-based Multi-Agent Systems using Discrete Event Simulation
#### Experiments for paper _"Testing BDI-based Multi-Agent Systems using Discrete Event Simulation"_ submitted to JAAMAS

#### Authors

**Martina Baiardi** (m.baiardi@unibo.it),
**Samuele Burattini** (samuele.burattini@unibo.it),
**Giovanni Ciatto** (giovanni.ciatto@unibo.it),
**Danilo Pianini** (danilo.pianini@unibo.it)

Department of Computer Science and Engineering
Alma Mater Studiorum --- Università di Bologna - Cesena, Italy

### Abstract

Multi-agent systems are designed to deal with open, 
distributed systems with unpredictable dynamics, 
which makes them inherently hard to test. 
The value of using simulation for this purpose is recognized in the literature, 
although achieving sufficient fidelity 
(i.e., the degree of similarity between the simulation and the real-world system) 
remains a challenging task. 
This is exacerbated when dealing with cognitive agent models, 
such as the Belief Desire Intention (BDI) model, 
where the agent codebase is not suitable to run unchanged in simulation environments, 
thus increasing the reality gap between the deployed and simulated systems. 
We argue that BDI developers should be able to test in simulation
the same specification that will be later deployed, 
with no surrogate representations. 
Thus, in this paper, 
we discuss how the control flow of BDI agents can be mapped onto a Discrete Event Simulation, 
showing that such integration is possible at different degrees of granularity. 
We substantiate our claims by producing an open-source prototype integration between 
two pre-existing tools (JaKtA and Alchemist), 
showing that it is possible to produce a simulation-based testing environment 
for distributed BDI agents, 
and that different degrees of detail in the mapping among the two tools may lead to
different trade-offs in terms of performance and precision.

### Goal and Scope
To demonstrate the feasibility of the ideas discussed in the paper, 
here we present a proof of concept implementation of a BDI interpreter 
supporting both real-world and simulation execution.
To do so, 
we adopt the [JaKtA BDI framework](https://github.com/jakta-bdi/jakta), 
which already supports real-world execution of MAS as concurrent applications, 
and it allows for plugging in different concurrency models. 
With this experiment,
we show how JaKtA can be extended to support one more way to
run agents, namely: in a simulated world, 
as created by the DES (Discrete-time Event Simulation) [Alchemist simulator](https://github.com/AlchemistSimulator/Alchemist).
This experiment demonstrates that: 
1. BDI systems’ execution can be swapped between real-world deployments and DES
  simulation without changing the MAS specification, 
2. Simulation granularity choices do impact the validation of a BDI system. 

To do so, we consider four possible granularity mappings:
1. **AMA** (**Atomic MAS Advancements**):
    Each DES event corresponds to a full control-loop iteration of all agents in the system,
    making it the most coarse-grained option. 
    One obvious consequence of this choice is that all agents in the MAS run at the same frequency, 
    thus inducing implicit synchronisation, 
    and discarding all the effects caused by possible interleaving agents’ actions.
2. **ACLI** (**Atomic Control-Loop Iterations**):
    Each DES event corresponds to a full control-loop iteration of a single agent, 
    making this option slightly more fine-grained than AMA. 
    In this case, arbitrary interleaves among agents’ actions are possible, 
    but not among different agents’ control-loop phases. 
    This option does not allow modelling different durations for different phases of
    the control loop, for instance, 
    it cannot be captured a situation in which deliberation takes too long and makes 
    the sensing phase outdated before an action is taken.
3. **ACLP** (**Atomic Control-Loop Phase**):
    Every single atomic phase (sense, deliberate and act) is represented as a DES event.
    This option preserves the behaviour of concurrent or distributed BDI agents,
    allowing phases to interleave across different agents,
    thus capturing complex inter-agent concurrency patterns.
4. **ABE** (**Atomic BDI Event**):
    Each BDI event is mapped to a single DES event. 
    This is the finest-grained option. 
    The approach has value for investigating the internals of the agent execution platform implementation, 
    e.g., to verify the correctness of the BDI interpreter implementation and its degree of parallelism, 
    but, from the point of view of an observer of the MAS, 
    this model and ACLP are indistinguishable, 
    as the phases of the control loop of each agent are atomic. 
    _**For this reason, in the remainder of the paper, we will consider ABE to be subsumed by ACLP.**_

We use a UAVs coordination scenario as our reference. 
We use `JaKtA` to instruct a flock of UAVs, 
each controlled by a BDI agent, 
to build and maintain a circular formation while following a moving leader UAV. 
The leader UAV moves in a circular path (radius `rl = 5m`), 
while follower UAVs must coordinate to build a circular formation around
the leader so that every follower is at distance `rf = 2.5m` from the leader, 
and all followers are equally distanced to each other. 
This formation must be maintained while the leader moves along its path.
We assume direct Line of Sight (LoS) UAV-to-UAV communication within a short
range of `rc = 5m`. UAVs can navigate the space at a maximum speed of `1m/s`. 
If no signal is received from the leader, followers hover at their current location. 
In this scenario, we assume UAVs are equipped with some localisation system that provides
them with exact coordinates of their position in a given shared reference system.
Initially, follower UAVs are randomly displaced into a circular arena of radius 10m,
while the leader is located at the arena centre

### Technologies adopted for the experiment

- The experiment is implemented in [Kotlin](https://kotlinlang.org/)
- The experiment is built using [Gradle](https://gradle.org/).
- BDI Agents are developed in [JaKtA](https://github.com/jakta-bdi/jakta).
- The simulator integrated is [Alchemist](https://alchemist.github.io/).

This artifact is generated, validated, and published with a GitHub Actions CI/CD pipeline,
its configuration is consultable in the file `.github/workflows/build-and-deploy.yml`.
After each commit on `main` branch, the **automatic** process performs, in order:
1. Checkout of latest code changes
2. Build of the source code
3. Static checks of source code (linters, class/methods documentation)
4. Execution of Unit tests
5. Execution of the artifact for a small amount of time, to verify it starts succesfully.
6. Generation of the charts
7. Computation of the release version based on [Conventional Commit](https://www.conventionalcommits.org/en/v1.0.0/)
   and performs the release on GitHub, including the generated charts. The configuration for this process is in `release.config.js` file.
8. Build of docker images and publish on DockerHub

This process ensures the reproducibility at every addition into the code base.

On top of this process,
dependencies are automatically updated using [Renovate](https://docs.renovatebot.com/) (configured in `renovate.json` file),
and automatically tested before their inclusion in the `master` branch.

### Project structure
Relevant files and folders for the experiment relevance in this project are:
```md
uav-circle-2024-jakta-alchemist
├── data/...                            # Data from previously executed simulations to generate charts
├── docker
│   ├── charts/Dockerfile               # Dockerfile that generated the charts
│   └── sim/Dockerfile                  # Dockerfile executing the simulation
├── effects/...                         # Graphic effects for simulation on GUI
├── src
│   ├── main
│   │   ├── kotlin/it/unibo/alchemist/...       # Alchemist simulator extensions
│   │   ├── kotlin/it/unibo/jakta/agents/...    # JaKtA BDI framework extensions
│   │   ├── kotlin/it/unibo/jakta/examples
│   │   │   ├── common/...                      # Experiment files shared among concurrent and Simulation Execution
│   │   │   ├── main                            # Concurrent execution files
│   │   │   │   ├── [...]
│   │   │   │   ├── FollowerDrone.kt            # Concurrent execution leader drone (== simulation)
│   │   │   │   ├── LeaderDrone.kt              # Concurrent execution follower drone (== simulation)
│   │   │   │   └── SwarmExecutionMain.kt       # Concurrent execution entrypoint
│   │   │   └── simulation/...
│   │   │       ├── [...]
│   │   │       ├── FollowerDrone.kt            # Simulated leader drone (== concurrent execution)
│   │   │       └── LeaderDrone.kt              # Simulated follower drone (== concurrent execution)
│   │   ├── yaml/1-agent-phase.yml              # ACLP simulation configuration file
│   │   ├── yaml/2-agent-lc.yml                 # ACLI simulation configuration file
│   │   └── yaml/3-sync-mas.yml                 # AMA simulation configuration file
│   └── test/...                                # Unit tests for verifying the correct circle movement  
├── docker-compose.yml                  
└── process.py                                  # Python script to generate charts from data/*
```

### Experiment configuration

We use the simulator to compare the execution of the same MAS logic with different
granularities, namely we consider AMA, ACLI, and ACLP. 

We investigate how the mapping granularity impacts the system’s behaviour. 
To do so, we use AMA as baseline, 
letting the entire MAS run a full cycle every simulated second 
(Dirac Comb distribution with frequency `f = 1Hz → T = 1s`), 
thus replicating the behaviour of most current agent-based simulation frameworks, 
which are time-driven. 
We compare the baseline with ACLI and ACLP, for which, 
we model each agent’s control-loop frequency following a Weibull distribution with mean `f` and deviation `f · τ` (drift). 
Intuitively, 
this means that most loops will be scheduled around the mean frequency `f`, 
but some loops will be scheduled earlier or later, 
thus introducing a drift `τ` in the agents’ execution.
Additionally, 
for the ACLP granularity we model deliberation and action delays,
associating each phase with an exponential distribution with rate `λ = f`: 
faster agents (larger `f` values) have less delay. 
This is needed to emulate a real-world concurrent deployment, 
in which different phases of the control loop may take different time to complete and may interleave.
Every experiment is repeated 100 times with a different random seed, 
changing the initial positions of the followers and the distribution in time of the events for the
ACLI and ACLP. 
In each experiment, the leader follows a circular trajectory of radius `r  = 5m` 
and is set to complete a full circle in 600s.
We let the system execute for 1500 simulated seconds.

There are three simulation configuration files, 
one for each granularity analysed, accordingly:
1. `src/main/resources/yaml/1-agent-phase.yml` for ACLI granularity;
2. `src/main/resources/yaml/2-agent-lc.yml` for ACLP granularity;
3. `src/main/resources/yaml/3-sync-mas.yml` for AMA granularity.

For the comparison on concurrent system,
the maximum allowed frequency `f` is determined by the host machine’s CPU capabilities. 
Any relative drift `τ` between different UAVs is naturally introduced by the operating system’s scheduler. 
The duration of each phase of the control loop depends on the actual computation time of the agent program,
which in turn is determined by the host machine’s CPU performance.

The entrypoint for executing the **same** BDI system on the concurrent system 
is `src/main/kotlin/it/unibo/jakta/examples/main/SwarmExecutionMain.kt`.

## Getting started

The experiment can be run in three four ways:
1. [Full-Batch-Run] the batched execution of the all 100 seeds of simulation, which generates raw data. This is the most time-consuming step, which produces the results available inside `data` folder. Executing this step requires approximately one week on a machine with 750GiB of RAM and 96 CPU cores.
2. [Graphical-Run] the simulation can be executed in a graphical mode, which allows to visualise the simulation in real-time. This is useful for debugging and understanding the simulation dynamics, but it is not suitable for generating results, since it runs only a single seed of the simulation and default value for the free variables. The number of UAVs chosen for the graphical execution is 6 since allows for better UI visualization.
3. [Concurrent-Execution] the experiment with the **same** BDI logic is executed on the host machine as concurrent system.
4. [Chart-Generation] the charts can be generated from the data already provided in `data` folder. This is a fast step, which produces the charts inside `charts` folder.

**Note:** There is no hardware requirement for running the experiment, the simulation will use the available hardware resources. Still, it is recommended to run the experiment on a computer with at least 16 GB of RAM and 8 CPU cores.

### [Full-Batch-Run] Reproduce the entire experiment
**WARNING**: re-running the whole experiment may take a very long time on a personal computer.

#### Reproduce with containers (recommended)
1. Install **Docker** and **docker-compose**
2. Run `docker-compose up`
3. The charts will be available in the `charts` folder.

#### Reproduce simualtion natively
1. Install a Gradle-compatible version of [**Java**](https://docs.oracle.com/en/java/javase/21/install/overview-jdk-installation.html).
   Use the [Gradle/Java compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html)
   to learn which is the compatible version range.
   The Version of Gradle used in this experiment can be found in the `gradle-wrapper.properties` file
   located in the `gradle/wrapper` folder.
2. Install the version of Python indicated in `.python-version` (or use `pyenv`).
3. Launch either:
    - `./gradlew runAllBatch` on Linux, MacOS, or Windows if a bash-compatible shell is available;
    - `gradlew.bat runAllBatch` on Windows cmd or Powershell;
4. Once the experiment is finished, the results will be available in the `data` folder. Then run:
    - `python -m venv venv`
    - `source venv/bin/activate`
    - `pip install --upgrade pip`
    - `pip install -r requirements.txt`
    - `python process.py`
5. The charts will be available in the `charts` folder.

> The batch execution by default will run the experiment with 16 drones, 
> and will run the simulation with the combination of:
> - `f = [1, 2, 3, 4, 5]` and 
> - `τ = [0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1]`


#### Reproduce concurrent execution natively
Follow the steps described for the simulation execution,
but instead of executing the gradle tasks described in 3, 
launch `./gradlew run`.

### [Graphical-Run] Run Single Graphical Experiment
1. Install a Gradle-compatible version of [**Java**](https://docs.oracle.com/en/java/javase/21/install/overview-jdk-installation.html).
   Use the [Gradle/Java compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html)
   to learn which is the compatible version range.
   The Version of Gradle used in this experiment can be found in the `gradle-wrapper.properties` file
   located in the `gradle/wrapper` folder.
2. Launch either:
    - `./gradlew <task>` on Linux, MacOS, or Windows if a bash-compatible shell is available;
    - `gradlew.bat <task>` on Windows cmd or Powershell;

Depending on the granularity to execute, the `<task>` to be specified is:
- `run1-agent-phaseGraphic`
- `run2-agent-lcGraphic`
- `run3-sync-masGraphic`

To start the simulation press `P`, you are free to pause/resume it by pressing `P` on keyboard.

The graphical execution launches the simulation with default values for the free variables and the random seed,
this can behaviour can be manually customized by changing the configuration in the corresponding `yml` file.

> The number of UAVs chosen for the __graphical__ execution is 6 since allows for better UI visualization.
> This number can be freely modified in the yaml file.

Currently default values for variables for graphical execution are:
- _variance_ (`τ`): 0.6 
- _agentFrequency_ (`f`): 1
- _numberOfDrones_: 6

To make changes to existing experiments and explore/reuse,
we recommend to use the **IntelliJ Idea IDE**.
Opening the project in IntelliJ Idea will automatically import the project, download the dependencies,
and allow for a smooth development experience.

### [Chart-Generation] Regenerate the charts

We keep a copy of the data in this repository,
so that the charts can be regenerated without having to run the experiment again.
To regenerate the charts, run `docker compose run --no-deps charts`.
Alternatively, follow the steps or the "reproduce natively" section,
starting from step 4.

The charts will be available in the `charts` folder.
