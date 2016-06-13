#include <stdint.h>
#include <stddef.h>

typedef uint64_t u64;

typedef struct {
	void *data;
} dictionary_t;

struct _object_t;
typedef struct _object_t object_t;

typedef enum {
	U64,
	STRING,
	INTERFACE,
	ARRAY
} attribute_type_t;

typedef struct {
	u64 type;
	void *data;
} attribute_value_t;

typedef struct {
	const char *name;
	attribute_value_t value;
} attribute_t;

typedef struct {
	const char *object_name;
	const char *interface_name;
	object_t *object;
	void *interface;
} attribute_value_interface_t;

typedef struct {
	u64 nr_fields;;
	attribute_value_t *fields;
} attribute_value_array_t;

typedef void (*callback_f)(object_t *o, void *usr_data);
typedef void* (*create_f)(object_t *o);

typedef struct {
	const char *module_name;
	const char *object_name;
	u64 nr_attributes;
	attribute_t *attributes;
} object_conf_t;

typedef struct {
	const char *name;
	void *so;

	u64 nr_attributes;
	attribute_t *attributes;

	create_f create;
	callback_f start;
	callback_f run_one_cycle;
	callback_f snapshot_module;
	callback_f restore_module;
	callback_f update_state;
	callback_f exit_object;
} module_t;

struct _object_t {
	const char* name;
	const object_conf_t *conf;
	module_t *module;
	dictionary_t interfaces;

	u64 nr_attributes;
	attribute_value_t *attributes;

	void *usr_data;
};
