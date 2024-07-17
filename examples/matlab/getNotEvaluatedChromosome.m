function [chromosome, isOptimizationFinished, shouldWait] = getNotEvaluatedChromosome(optimizationRunId)

request = matlab.net.http.RequestMessage('GET');
[response, completedResponse, ~] = send(request, 'localhost:8080/optimizationRun/'+string(optimizationRunId)+'/chromosome/notEvaluated');

if(response.StatusCode == 102)
    chromosome = 0;
    isOptimizationFinished = false;
    shouldWait = true;
    return
else
    shouldWait = false;
end

if(completedResponse.Completed)
    data = response.Body.Data;
    isOptimizationFinished = strcmp(data.optimizationStatus,'FINISHED');
    chromosome = data.chromosome;
end

end

