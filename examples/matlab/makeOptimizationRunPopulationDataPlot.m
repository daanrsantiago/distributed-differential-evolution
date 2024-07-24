close all
clear
clc

%% Get OptimizationRun Data

optimizationRunId = 1;

optimizationRunData = getOptimizationRunById(optimizationRunId);

%% Get populations data

populationIds = zeros(1,optimizationRunData.maxGenerations);
populationsStatistics = zeros(4,optimizationRunData.maxGenerations);

page = 0;
iPopulation = 1;

while true
    
    optimizationRunPopulationsPage = getOptimizationRunPopulations(optimizationRunId,page); 
    
    for iPopulationPageItem = 1:length(optimizationRunPopulationsPage.content)
        populationPageItemStatistics = optimizationRunPopulationsPage.content(iPopulationPageItem).statistics;
        populationsStatistics(1, iPopulation) = populationPageItemStatistics.generation;
        populationsStatistics(2, iPopulation) = populationPageItemStatistics.bestFitness;
        populationsStatistics(3, iPopulation) = populationPageItemStatistics.meanFitness;
        populationsStatistics(4, iPopulation) = populationPageItemStatistics.worstFitness;
        iPopulation = iPopulation + 1;
    end
    
    if (optimizationRunPopulationsPage.last)
        break
    end
    
    page = page + 1;
    
end

plot(populationsStatistics(1,:), populationsStatistics(2, :))
hold on
plot(populationsStatistics(1,:), populationsStatistics(3, :))
plot(populationsStatistics(1,:), populationsStatistics(4, :))
legend('bestFitness', 'meanFitness', 'worstFitness')
title('optimization statistics')
xlabel('generation')
ylabel('fitness')
grid minor