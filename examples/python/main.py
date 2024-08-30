from optimization_api import get_not_evaluated_chromosome
from optimization_api import post_evaluation_result
from objective_function import objective_function

if __name__ == "__main__":
    optimization_run_id = 1

    while True:
        chromosome_response = get_not_evaluated_chromosome(optimization_run_id)
        chromosome = chromosome_response['chromosome']

        if chromosome_response['optimizationStatus'] == 'RUNNING':
            fitness = objective_function(chromosome['elements'])
            post_evaluation_result(
                chromosome_id=chromosome['id'],
                evaluation_id= chromosome['evaluationId'], 
                fitness=fitness
            )
        else:
            break