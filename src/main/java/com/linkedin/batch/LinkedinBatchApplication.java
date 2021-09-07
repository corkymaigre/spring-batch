package com.linkedin.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@SpringBootApplication
@EnableBatchProcessing
public class LinkedinBatchApplication {

    public static String[] tokens = new String[]{"order_id", "first_name", "last_name", "email", "cost", "item_id", "item_name", "ship_date"};

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    public static void main(String[] args) {
        SpringApplication.run(LinkedinBatchApplication.class, args);
    }

    @Bean
    public JobExecutionDecider deliveryDecider() {
        return new DeliveryDecider();
    }

    @Bean
    public JobExecutionDecider receiptDecider() {
        return new ReceiptDecider();
    }

    @Bean
    public ItemReader<Order> itemReader() {
        FlatFileItemReader<Order> itemReader = new FlatFileItemReader<>();
        itemReader.setLinesToSkip(1);
        itemReader.setResource(new FileSystemResource("data/shipped_orders.csv"));

        DefaultLineMapper<Order> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(tokens);

        lineMapper.setLineTokenizer(tokenizer);

        lineMapper.setFieldSetMapper(new OrderFieldSetMapper());

        itemReader.setLineMapper(lineMapper);
        return itemReader;

    }

    @Bean
    public Step chunkBasedStep() {
        return this.stepBuilderFactory.get("chunkBasedStep")
                .<Order, Order>chunk(3)
                .reader(itemReader())
                .writer(list -> {
                    System.out.printf("Received list of size: %s%n", list.size());
                    list.forEach(System.out::println);
                }).build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("job").start(chunkBasedStep()).build();
    }

    @Bean
    public Step nestedBillingJobStep() {
        return this.stepBuilderFactory.get("nestedBillingJobStep").job(billingJob()).build();
    }

    @Bean
    public Step sendInvoiceStep() {
        return this.stepBuilderFactory.get("sendInvoiceStep").tasklet((stepContribution, chunkContext) -> {
            System.out.println("Invoice is sent to the customer");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Flow billingFlow() {
        return new FlowBuilder<SimpleFlow>("billingFlow").start(sendInvoiceStep()).build();
    }

    @Bean
    public Job billingJob() {
        return this.jobBuilderFactory.get("billingJob").start(sendInvoiceStep()).build();
    }

    @Bean
    public Flow deliveryFlow() {
        return new FlowBuilder<SimpleFlow>("deliveryFlow").start(driveToAddressStep())
                .on("FAILED").fail()
                .from(driveToAddressStep())
                .on("*").to(deliveryDecider())
                .on("PRESENT").to(givePackageToCustomerStep())
                .next(receiptDecider()).on("CORRECT").to(thankCustomerStep())
                .from(receiptDecider()).on("INCORRECT").to(refundStep())
                .from(deliveryDecider())
                .on("NOT_PRESENT").to(leaveAtDoorStep()).build();
    }

    @Bean
    public StepExecutionListener selectFlowerListener() {
        return new FlowersSelectionStepExecutionListener();
    }

    @Bean
    public Step selectFlowersStep() {
        return this.stepBuilderFactory.get("selectFlowersStep").tasklet((stepContribution, chunkContext) -> {
            System.out.println("Gathering flowers for order.");
            return RepeatStatus.FINISHED;
        }).listener(selectFlowerListener()).build();
    }

    @Bean
    public Step removeThornsStep() {
        return this.stepBuilderFactory.get("removeThornsStep").tasklet((stepContribution, chunkContext) -> {
            System.out.println("Remove thorns from roses.");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step arrangeFlowersStep() {
        return this.stepBuilderFactory.get("arrangeFlowersStep").tasklet((stepContribution, chunkContext) -> {
            System.out.println("Arranging flowers for order.");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Job prepareFlowersJob() {
        return this.jobBuilderFactory.get("prepareFlowersJob")
                .start(selectFlowersStep())
                .on("TRIM_REQUIRED").to(removeThornsStep()).next(arrangeFlowersStep())
                .from(selectFlowersStep()).on("NO_TRIM_REQUIRED").to(arrangeFlowersStep())
                .from(arrangeFlowersStep()).on("*").to(deliveryFlow())
                .end()
                .build();
    }

    @Bean
    public Step thankCustomerStep() {
        return this.stepBuilderFactory.get("thankCustomerStep").tasklet((stepContribution, chunkContext) -> {
            System.out.println("Thanking the customer.");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step refundStep() {
        return this.stepBuilderFactory.get("refundStep").tasklet((stepContribution, chunkContext) -> {
            System.out.println("Refunding customer money.");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step leaveAtDoorStep() {
        return this.stepBuilderFactory.get("leaveAtDoorStep").tasklet((stepContribution, chunkContext) -> {
            System.out.println("Leaving the package at the door.");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step storePackageStep() {
        return this.stepBuilderFactory.get("storePackageStep").tasklet((stepContribution, chunkContext) -> {
            System.out.println("Storing the package while the customer address is located");
            return RepeatStatus.FINISHED;
        }).build();
    }


    @Bean
    public Step givePackageToCustomerStep() {
        return this.stepBuilderFactory.get("givePackageToCustomerStep").tasklet((stepContribution, chunkContext) -> {
            System.out.println("Given the package to the customer");
            return RepeatStatus.FINISHED;
        }).build();
    }


    @Bean
    public Step driveToAddressStep() {
        boolean isLost = false;
        return this.stepBuilderFactory.get("driveToAddressStep").tasklet((stepContribution, chunkContext) -> {
            if (isLost) throw new RuntimeException("Got lost driving to the address");
            System.out.println("Successfully arrived at the address.");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step packageItemStep() {
        return this.stepBuilderFactory.get("packageItemStep").tasklet((stepContribution, chunkContext) -> {
            String item = chunkContext.getStepContext().getJobParameters().get("item").toString();
            String date = chunkContext.getStepContext().getJobParameters().get("run.date").toString();
            System.out.printf("The %s has been packaged on %s.%n", item, date);
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Job deliverPackageJob() {
        return this.jobBuilderFactory.get("deliverPackageJob")
                .start(packageItemStep())
                .split(new SimpleAsyncTaskExecutor())
                .add(deliveryFlow(), billingFlow())
                .end()
                .build();
    }

}
