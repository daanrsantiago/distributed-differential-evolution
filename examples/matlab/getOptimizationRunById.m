function [optimizationRunData] = getOptimizationRunById(optimizationRunId)

request = matlab.net.http.RequestMessage('GET');
[response, completedResponse, ~] = send(request, 'localhost:8080/optimizationRun/'+string(optimizationRunId));

if(completedResponse.Completed)
    optimizationRunData = response.Body.Data;
end

end

