import sys
import pandas as pd

file_path = sys.argv[1]
df = pd.read_csv(file_path)

movie_list_df = df[df["URL"].astype(str).str.contains("/movie-list", na=False)]

movie_list_df["TS_TIME"] = pd.to_numeric(movie_list_df["TS_TIME"], errors="coerce")
movie_list_df["TJ_TIME"] = pd.to_numeric(movie_list_df["TJ_TIME"], errors="coerce")

print("Average TS_TIME:", movie_list_df["TS_TIME"].mean() / 1_000_000)
print("Average TJ_TIME:", movie_list_df["TJ_TIME"].mean()/ 1_000_000)