# LearnSDD
This is the implementation of the LearnSDD algorithm that learns a tractable Markov Network structure with a tractable represenation, as presented in:

```
@inproceedings{bekker2015tractable,
  title={Tractable learning for complex probability queries},
  author={Bekker, Jessa and Davis, Jesse and Choi, Arthur and Darwiche, Adnan and Van den Broeck, Guy},
  booktitle={Advances in Neural Information Processing Systems},
  pages={2242--2250},
  year={2015}
} 
```

# Dependencies and System Requirements
- sdd 2.0 library: Download from http://reasoning.cs.ucla.edu/sdd/
- JSDD.jar: The Java wrapper for the sdd library. It is already included in lib/, but also available on https://github.com/jessa/JSDD (already included in LearnSDD.jar)
- jgrapht: Graph library (http://jgrapht.org/) (already included in LearnSDD.jar)
- commonsio: IO commons (https://commons.apache.org/proper/commons-io/) (already included in LearnSDD.jar)
- l-bfgs: Limited-memory BFGS implementation. It is already included, but is also available on (www.chokkan.org/software/liblbfgs/)

The sdd library works on linux and OS X. The java wrapper is only tested on linux, but should also work on OS X.


# Quickstart
1. From the sdd package, copy the sdd library (libsdd.so) to lib/

2. Make sure that the c libraries can be found by adding lib/ to the ld library path
    ```
    export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:lib/
    ```
    
3. Learn a model for the NLTCS dataset and store the output in the directory
nltcs output/:
    ```
    java -jar LearnSDD.jar learn data/nltcs.train.wdata data/nltcs.valid.wdata
    nltcs_output
    ```

4. Infer the dataset log likelihood and the size of the NLTCS model:
    ```
    java -jar LearnSDD.jar infer nltcs_output/best/model.sdd nltcs_output/best/vtree.vtree nltcs_output/best/weights.csv data/nltcs.test.wdata
    ```

For more information about the commands, use the following commands:
```
java -jar LearnSDD.jar
java -jar LearnSDD.jar learn
java -jar LearnSDD.jar infer
```

# Options for Learning
- alpha: The trade-off parameter of the scoring function, default = 0.1
- maxEdge: The maximum number of edges for the learned SDD, default = 10^9
- features: The types of features that are generated (conj, mutex or conj-mutex), default = conj.
- verbose: if this flag is set, all the intermediate models and more lag files are saved.


# File Formats

## Data Files
Every line is an example and every examples consists of binary variables, separated by by kommas. The following dataset, for example, has 6 examples with 16 variables:

0,0,0,1,1,1,0,1,0,1,1,1,1,1,1,0
0,0,0,1,1,0,0,0,0,0,0,0,0,1,0,0
0,0,0,1,1,1,1,1,1,1,0,1,0,1,1,0
0,0,0,0,1,0,0,0,0,1,1,1,0,1,1,0
0,0,0,1,1,1,0,1,0,1,1,1,0,1,0,0
0,0,0,0,1,1,0,0,0,1,0,1,0,1,0,0

Optionally, the examples can be weighted. In that case the weight is stated at the beginning of the line and the weight and example are separated by a bar:

21|0,0,0,1,1,1,0,1,0,1,1,1,1,1,1,0
30|0,0,0,1,1,0,0,0,0,0,0,0,0,1,0,0
26|0,0,0,1,1,1,1,1,1,1,0,1,0,1,1,0
19|0,0,0,0,1,0,0,0,0,1,1,1,0,1,1,0
68|0,0,0,1,1,1,0,1,0,1,1,1,0,1,0,0
43|0,0,0,0,1,1,0,0,0,1,0,1,0,1,0,0

## SDD and Vtree Files
The standard format is used. The format is explained at the beginning of each file.

## Weights Files
To do weighted model counting with SDDs, a weight needs to be provided for each SDD variable. A weights file has a line for each weight and the weight is stored in log space. The line number is the variable index that the weights corresponds to. Because the SDD variables start at 1, the first line is placehold number that will not be used. The following example is a weights file for an SDD that has 5 variables:

0.0
0.95
-0.32
0.06
-0.78
-0.59

# Output Directory
For learning, an output directory needs to be provided. In this directory the
following files and directories will appear:

- **arguments.txt**: The arguments that were set for learning this model.
- **results.csv**: This file contains a line for each iteration and for each
iterations it gives the time that iteration took (not accumulated), and the
following information of the model learned in that iteration: the training
set log likelihood, validation set loglikelihood and SDD size (number of
edges).
3- best/: The directory with the model that has the best validation set log
likelihood.
- **best/model.sdd**: SDD of the model
- **best/model.dot**: Visualization of the SDD of the model
- **best/vtree.vtree**: Vtree of the model
- **best/vtree.dot**: Visualization of the vtree of the model
- **best/weights.csv**: The weights of the model.
- **best/features.txt**: The features of the model, written in an under-
standable way.
- **best/iteration**: The iteration that produced this model.

If the flag --verbose is set, then additionally these files are created:
 - **log**: A log file
 - **<it>/**: For each iteration a folder is created with the model of that
iteration. It contains the same files as best/

# Brief Code Walk-through
The code is accessed through learnsdd.LearnSdd which parses the arguments and either executes inference on a model (using learnsdd.InferenceModel) or learning.

learnsdd.Learner contains the learning algorithm and takes care of the output. The algorithm starts with an initial model (learnsdd.Model) and updates it iteratively. In each iteration candidate features are generated and tested one by one. The best feature is finally added to the model.

A feature is a logical formula over other features and variables (logic.BooleanFormula and its subclasses Literal, Conjunction, ConjunctionOfLiterals, ConjunctionOfTwoLiterals, Disjunction, ExactOne, Mutex). Note that we do not distinguish between model variables and model features, as a variable is a specific type of feature, and for each feature a new variable is created that is positive if the example has the feature and negative otherwise. A logical formula can also be represented by an SDD (logic.SddFormula).

The features can be generated by two generators: conjunctive feature generator and mutex feature generator. The conjunctive feature generator (featuregenerator.ConjunctiveFeatureGenerator) generates features that are the conjunction of two existing features, possibly with the feature negated. Such a feature is represented by logic.ConjunctionOfTwoLiterals. The mutex feature generator (featuregenerator.MutexFeatureGenerator) generates features that represent sets of mutually exclusive variables (logic.Mutex), optionally with the additional rule that there is always a positive variable, like for multivalued variables that are binarized (logic.ExactOne).

Because the number of candidate features can be extremely high, only a subset of them are actually tested. The subset is selected using a featurefilter.FeatureFilter. The mutex and exact-1 features are filtered randomly (featurefilter.RandomFilter). The conjunctive features are selected on their Kullback-Leibler divergence (KLD) between the data and the model. The more they diverge, the more likely it is a good feature. To optimize the KLD computations, a partially ordered list of the features is kept in between iterations. We assume that the KLD only decreases as the model improves (correct most of the time), and therefore features only need to be rescored when their old score would put it in the top k features. The implementation of the partially sorted list is featurefilter.PartialFilter. The KLD of conjunctive features can be computed for many features simultaneously, this is implemented in featurefilter.ConjKLDScorer.

To test a feature, it is temporarily added to the model and scored based on the training set log likelihood improvement and relative increase in number of edges. The datasets are represented by data.Data, where each instance is a data.Instance object. For practical reasons, each feature is added to the datasets as a variable. Therefore, the datasets are kept in the model (learnsdd.Model). Each time a feature is added, the weights of alle the features need to be relearned, this is the task of weightlearner.LbfgsWeightLearner. The weightlearner is implemented in c and accessed using Java Native Interface (JNI).

# Weight Learning Library
Weight learning is implemented in c. The directory lib/ already contains the compiled library, but the source code is also available in weightlearner/. The implementation uses the Limited-memory BFGS implementation of Jorge Nocedal and Naoaki Okazaki (www.chokkan.org/software/liblbfgs/).

# Contact
If you have further questions, do not hesitate to contact Jessa Bekker (https:
//people.cs.kuleuven.be/~jessa.bekker/)

# Cite
Please cite the original paper when you use the code:

```
@inproceedings{bekker2015tractable,
  title={Tractable learning for complex probability queries},
  author={Bekker, Jessa and Davis, Jesse and Choi, Arthur and Darwiche, Adnan and Van den Broeck, Guy},
  booktitle={Advances in Neural Information Processing Systems},
  pages={2242--2250},
  year={2015}
} 
```