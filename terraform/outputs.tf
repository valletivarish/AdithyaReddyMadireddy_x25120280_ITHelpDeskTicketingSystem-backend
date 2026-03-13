# Output values for the deployed infrastructure
# These are displayed after terraform apply and can be used in CI/CD pipelines

output "ec2_public_ip" {
  description = "Public IP address of the backend EC2 instance"
  value       = aws_instance.helpdesk_backend.public_ip
}

output "ec2_public_dns" {
  description = "Public DNS of the backend EC2 instance"
  value       = aws_instance.helpdesk_backend.public_dns
}

output "rds_endpoint" {
  description = "Connection endpoint for the RDS PostgreSQL database"
  value       = aws_db_instance.helpdesk_db.endpoint
}

output "rds_database_name" {
  description = "Name of the RDS database"
  value       = aws_db_instance.helpdesk_db.db_name
}

output "s3_website_url" {
  description = "URL for the S3-hosted frontend website"
  value       = aws_s3_bucket_website_configuration.frontend_website.website_endpoint
}

output "s3_bucket_name" {
  description = "Name of the S3 bucket for frontend deployment"
  value       = aws_s3_bucket.helpdesk_frontend.id
}
