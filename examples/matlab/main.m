clear all
close all

%% Loop

% p = gcp

while true
   
    [chromosome, isOptimizationFinished, shouldWait] = getNotEvaluatedChromosome(1);
    
    if (isOptimizationFinished)
        break
    elseif (shouldWait)
        pause(0.5)
        continue
    end
    
    result = rastrigin(chromosome.elements);
%     parfeval(p,@publishEvaluationResult,0,result,chromosome);
    publishEvaluationResult(result, chromosome)
    
end